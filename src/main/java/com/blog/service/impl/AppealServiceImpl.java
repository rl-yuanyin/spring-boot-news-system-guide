package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Appeal;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.AppealMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.AppealService;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {

    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    public void submit(String username, String reason) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (user.getStatus() != 1) throw BusinessException.badRequest("您的账号未被封禁，无需申诉");
        // 已有待处理的申诉
        Appeal existing = getOne(new LambdaQueryWrapper<Appeal>()
                .eq(Appeal::getUsername, username)
                .eq(Appeal::getStatus, 0));
        if (existing != null) throw BusinessException.badRequest("已有待处理的申诉，请耐心等待");
        Appeal appeal = Appeal.builder().username(username).reason(reason).status(0).reply("").build();
        save(appeal);
    }

    @Override
    public Appeal getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<Appeal>()
                .eq(Appeal::getUsername, username)
                .orderByDesc(Appeal::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public Page<Appeal> listAppeals(int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Appeal>().orderByDesc(Appeal::getCreateTime));
    }

    @Override
    public void approve(Long id) {
        Appeal appeal = getById(id);
        if (appeal == null) throw BusinessException.notFound("申诉不存在");
        appeal.setStatus(1);
        updateById(appeal);
        // 解封用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, appeal.getUsername()));
        if (user != null) {
            user.setStatus(0);
            userMapper.updateById(user);
            notificationService.send(user.getId(), "admin_delete",
                    "你的申诉已通过，账号已解封", null);
        }
    }

    @Override
    public void reject(Long id, String reply) {
        Appeal appeal = getById(id);
        if (appeal == null) throw BusinessException.notFound("申诉不存在");
        appeal.setStatus(2);
        appeal.setReply(reply);
        updateById(appeal);
    }
}
