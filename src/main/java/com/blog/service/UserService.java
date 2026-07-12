package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.User;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return 注册成功的用户信息
     */
    UserVO register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return 登录成功后的 token + 用户信息
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
}
