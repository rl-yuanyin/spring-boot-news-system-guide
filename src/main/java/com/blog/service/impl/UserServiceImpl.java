package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.Constants;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import com.blog.utils.JwtUtils;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ArticleMapper articleMapper;

    @Override
    public UserVO register(RegisterDTO dto) {
        // 校验用户名唯一
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw BusinessException.badRequest("用户名已存在");
        }

        // 构建用户
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername())
                .email(dto.getEmail())
                .role(0)   // 默认普通用户
                .status(0) // 默认正常
                .build();

        save(user);
        return UserVO.fromEntity(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查用户
        User user = getByUsername(dto.getUsername());
        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 1) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 生成 JWT
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginVO.builder()
                .token(token)
                .user(UserVO.fromEntity(user))
                .build();
    }

    @Override
    public UserVO getUserVOById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return UserVO.fromEntity(user);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public Page<UserVO> listUsers(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        page = page(page, new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime));
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(UserVO::fromEntity)
                .toList());
        return voPage;
    }

    @Override
    public void toggleUserStatus(Long id, int operatorRole) {
        User user = getById(id);
        if (user == null) throw BusinessException.notFound("用户不存在");
        // 超管不可被任何人禁用
        if (user.getRole() != null && user.getRole() == Constants.ROLE_SUPER_ADMIN) {
            throw BusinessException.badRequest("不能禁用超级管理员");
        }
        // 普通管理员只能被超管禁用
        if (user.getRole() != null && user.getRole() == Constants.ROLE_ADMIN && operatorRole < Constants.ROLE_SUPER_ADMIN) {
            throw BusinessException.badRequest("仅超级管理员可禁用管理员账号");
        }
        user.setStatus(user.getStatus() == 0 ? 1 : 0);
        updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.badRequest("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public void updateProfile(Long userId, String nickname, String email) {
        User user = getById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (nickname != null && !nickname.isBlank()) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        updateById(user);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        long articleCount = articleMapper.selectCount(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED));
        // 统计总浏览量
        var articles = articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED));
        long totalViews = articles.stream().mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum();
        return Map.of("articleCount", articleCount, "totalViews", totalViews);
    }

    @Override
    public void setUserRole(Long userId, Integer role) {
        User user = getById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (user.getRole() != null && user.getRole() == Constants.ROLE_SUPER_ADMIN) {
            throw BusinessException.badRequest("不能修改超级管理员的角色");
        }
        user.setRole(role);
        updateById(user);
    }
}
