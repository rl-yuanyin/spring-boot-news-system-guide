package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.FileRecord;
import com.blog.exception.BusinessException;
import com.blog.service.FileService;
import com.blog.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传、查询、删除")
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "上传文件", description = "登录用户上传图片，支持 jpg/png/jpeg/gif，最大 10MB")
    @PostMapping("/upload")
    public Result<FileRecord> upload(@RequestParam("file") MultipartFile file) {
        Long userId = requireLogin();
        return Result.success("上传成功", fileService.upload(file, userId));
    }

    @Operation(summary = "文件列表", description = "查询所有文件记录")
    @GetMapping
    public Result<List<FileRecord>> list() {
        return Result.success(fileService.listAll());
    }

    @Operation(summary = "删除文件", description = "上传者或管理员删除文件（同时删除磁盘文件）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = requireLogin();
        fileService.delete(id, userId);
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
