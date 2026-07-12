package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.CommentDTO;
import com.blog.exception.BusinessException;
import com.blog.service.CommentService;
import com.blog.utils.JwtUtils;
import com.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 评论控制器
 */
@Tag(name = "评论管理", description = "评论发布、查询、删除")
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "查询文章评论列表", description = "公开接口，按时间倒序")
    @GetMapping("/articles/{articleId}/comments")
    public Result<List<CommentVO>> list(@PathVariable Long articleId) {
        return Result.success(commentService.listByArticleId(articleId));
    }

    @Operation(summary = "发布评论", description = "登录用户对文章发表评论")
    @PostMapping("/comments")
    public Result<CommentVO> create(@Valid @RequestBody CommentDTO dto) {
        Long userId = requireLogin();
        return Result.success("评论成功", commentService.create(dto, userId));
    }

    @Operation(summary = "删除评论", description = "用户删除自己的评论，管理员删除任意")
    @DeleteMapping("/comments/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = requireLogin();
        commentService.delete(id, userId);
        return Result.success("删除成功", null);
    }

    // ========================================
    // 权限辅助
    // ========================================

    private String extractToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        String authHeader = attrs.getRequest().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Long requireLogin() {
        String token = extractToken();
        if (token == null || !jwtUtils.validateToken(token)) {
            throw BusinessException.unauthorized("请先登录");
        }
        return jwtUtils.getUserId(token);
    }
}
