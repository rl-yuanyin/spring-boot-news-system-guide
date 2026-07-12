package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Notification;
import com.blog.exception.BusinessException;
import com.blog.service.NotificationService;
import com.blog.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

@Tag(name = "通知消息", description = "点赞、评论、删文通知")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<List<Notification>> list() {
        Long userId = requireLogin();
        return Result.success(notificationService.listByUser(userId));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread")
    public Result<Map<String, Long>> unread() {
        Long userId = requireLogin();
        return Result.success(Map.of("count", notificationService.unreadCount(userId)));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        Long userId = requireLogin();
        notificationService.markRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        Long userId = requireLogin();
        notificationService.markAllRead(userId);
        return Result.success();
    }

    private String extractToken() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        String h = attrs.getRequest().getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? h.substring(7) : null;
    }

    private Long requireLogin() {
        String token = extractToken();
        if (token == null || !jwtUtils.validateToken(token))
            throw BusinessException.unauthorized("请先登录");
        return jwtUtils.getUserId(token);
    }
}
