package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Notification;
import com.blog.exception.BusinessException;
import com.blog.mapper.NotificationMapper;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public List<Notification> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime));
    }

    @Override
    public long unreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long id, Long userId) {
        Notification n = getById(id);
        if (n == null || !n.getUserId().equals(userId)) {
            throw BusinessException.badRequest("通知不存在");
        }
        n.setIsRead(1);
        updateById(n);
    }

    @Override
    public void markAllRead(Long userId) {
        var wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        List<Notification> list = list(wrapper);
        list.forEach(n -> n.setIsRead(1));
        updateBatchById(list);
    }

    @Override
    public void send(Long userId, String type, String content, Long articleId) {
        Notification n = Notification.builder()
                .userId(userId).type(type).content(content)
                .articleId(articleId).isRead(0).build();
        save(n);
    }
}
