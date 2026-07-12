package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleCreateDTO;
import com.blog.dto.ArticleQueryDTO;
import com.blog.entity.Article;
import com.blog.exception.BusinessException;
import com.blog.service.ArticleService;
import com.blog.utils.JwtUtils;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 文章管理控制器
 */
@Tag(name = "文章管理", description = "文章的发布、查询、修改、删除")
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final JwtUtils jwtUtils;

    // ========================================
    // 公开接口
    // ========================================

    @Operation(summary = "分页查询文章列表", description = "默认查询已发布文章，可按关键词、分类筛选")
    @GetMapping
    public Result<PageResult<ArticleVO>> list(ArticleQueryDTO dto) {
        // 用户可选登录，如果带了 token 则解析
        Long userId = getLoginUserId();
        return Result.success(articleService.pageQuery(dto, userId));
    }

    @Operation(summary = "热门文章排行榜", description = "基于 Redis ZSet 浏览量排行，公开接口")
    @GetMapping("/hot")
    public Result<List<ArticleVO>> hot(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(articleService.getHotArticles(limit));
    }

    @Operation(summary = "查询文章详情", description = "浏览文章详情，浏览量自动 +1")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        ArticleDetailVO vo = articleService.getDetail(id);
        // 非已发布文章，需要登录才能查看
        if (vo.getStatus() != 2) { // 2 = 已发布
            Long userId = requireLogin();
            if (userId != 1) {
                // 非作者且非管理员不能看非发布文章
                if (!vo.getUserId().equals(userId) && !isAdmin()) {
                    throw BusinessException.notFound("文章不存在");
                }
            }
        }
        return Result.success(vo);
    }

    // ========================================
    // 需要登录的接口
    // ========================================

    @Operation(summary = "发布文章", description = "登录用户发布文章，draft=true 保存草稿，否则提交审核")
    @PostMapping
    public Result<Article> create(@Valid @RequestBody ArticleCreateDTO dto) {
        Long userId = requireLogin();
        return Result.success(
                Boolean.TRUE.equals(dto.getDraft()) ? "草稿已保存" : "已提交审核",
                articleService.create(dto, userId));
    }

    @Operation(summary = "修改文章", description = "作者本人或管理员可修改")
    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable Long id, @Valid @RequestBody ArticleCreateDTO dto) {
        Long userId = requireLogin();
        return Result.success("修改成功", articleService.update(id, dto, userId));
    }

    @Operation(summary = "删除文章", description = "作者本人或管理员可删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = requireLogin();
        articleService.delete(id, userId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞/取消点赞", description = "登录用户对文章点赞（已赞则取消）")
    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean liked = articleService.toggleLike(id, userId);
        return Result.success(liked ? "点赞成功" : "已取消点赞", liked);
    }

    @Operation(summary = "提交审核", description = "作者将草稿或驳回的文章提交审核")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        Long userId = requireLogin();
        articleService.submit(id, userId);
        return Result.success("已提交审核", null);
    }

    // ========================================
    // 权限辅助方法
    // ========================================

    /**
     * 从请求头解析 JWT token
     */
    private String extractToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        String authHeader = attrs.getRequest().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取已登录用户 ID（未登录返回 null，不抛异常）
     */
    private Long getLoginUserId() {
        String token = extractToken();
        if (token != null && jwtUtils.validateToken(token)) {
            return jwtUtils.getUserId(token);
        }
        return null;
    }

    /**
     * 要求必须登录，返回用户 ID
     */
    private Long requireLogin() {
        String token = extractToken();
        if (token == null || !jwtUtils.validateToken(token)) {
            throw BusinessException.unauthorized("请先登录");
        }
        return jwtUtils.getUserId(token);
    }

    /**
     * 判断当前请求是否管理员
     */
    private boolean isAdmin() {
        String token = extractToken();
        if (token != null && jwtUtils.validateToken(token)) {
            return jwtUtils.getRole(token) == 1;
        }
        return false;
    }
}
