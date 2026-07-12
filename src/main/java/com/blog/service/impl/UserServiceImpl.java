package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import com.blog.utils.JwtUtils;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

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
}
