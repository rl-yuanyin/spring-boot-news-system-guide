package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.CategoryDTO;
import com.blog.entity.Category;
import com.blog.exception.BusinessException;
import com.blog.service.CategoryService;
import com.blog.utils.JwtUtils;
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
 * 分类管理控制器
 */
@Tag(name = "分类管理", description = "文章分类的增删改查")
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "查询分类列表", description = "所有人可访问")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "新增分类", description = "仅管理员")
    @PostMapping
    public Result<Category> add(@Valid @RequestBody CategoryDTO dto) {
        checkAdmin();
        return Result.success("新增成功", categoryService.add(dto));
    }

    @Operation(summary = "修改分类", description = "仅管理员")
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        checkAdmin();
        return Result.success("修改成功", categoryService.update(id, dto));
    }

    @Operation(summary = "删除分类", description = "仅管理员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 校验管理员权限（手动解析 token，不依赖拦截器的 ThreadLocal）
     */
    private void checkAdmin() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        String authHeader = attrs.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("请先登录");
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            throw BusinessException.unauthorized("token 无效或已过期");
        }
        if (jwtUtils.getRole(token) != 1) {
            throw BusinessException.forbidden("仅管理员可操作");
        }
    }
}
