package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.User;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 根据 ID 获取用户视图
     */
    UserVO getUserVOById(Long id);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 分页查询用户列表（管理员）
     */
    Page<UserVO> listUsers(int page, int pageSize);

    /**
     * 切换用户启用/禁用状态（管理员）
     * @param id 目标用户 ID
     * @param operatorRole 操作者角色（用于权限判断）
     */
    void toggleUserStatus(Long id, int operatorRole);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新个人信息（昵称、邮箱）
     */
    void updateProfile(Long userId, String nickname, String email);

    /**
     * 获取用户统计数据（文章数、总浏览量）
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 设置用户角色（仅超管）
     */
    void setUserRole(Long userId, Integer role);
}
