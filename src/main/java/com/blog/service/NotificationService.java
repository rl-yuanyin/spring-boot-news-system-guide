package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    /** 查询用户的通知列表（按时间倒序） */
    List<Notification> listByUser(Long userId);

    /** 未读数量 */
    long unreadCount(Long userId);

    /** 标记为已读 */
    void markRead(Long id, Long userId);

    /** 全部标记已读 */
    void markAllRead(Long userId);

    /** 发送通知 */
    void send(Long userId, String type, String content, Long articleId);
}
