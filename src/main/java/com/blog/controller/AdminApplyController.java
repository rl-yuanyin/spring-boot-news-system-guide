package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.entity.AdminApply;
import com.blog.service.AdminApplyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员申请", description = "用户申请成为管理员与超管审核")
@RestController
@RequiredArgsConstructor
public class AdminApplyController {

    private final AdminApplyService adminApplyService;

    @Operation(summary = "提交管理员申请")
    @PostMapping("/api/admin-apply")
    public Result<Void> submit(@RequestBody AdminApply body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        adminApplyService.submit(userId, body.getReason());
        return Result.success("申请已提交", null);
    }

    @Operation(summary = "查看我的申请")
    @GetMapping("/api/admin-apply/my")
    public Result<AdminApply> myApply() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.success(adminApplyService.getByUserId(userId));
    }

    @Operation(summary = "申请列表", description = "超管查看所有管理员申请")
    @GetMapping("/api/admin/admin-applies")
    public Result<Page<AdminApply>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminApplyService.listApplies(page, pageSize));
    }

    @Operation(summary = "通过申请", description = "超管通过管理员申请")
    @PutMapping("/api/admin/admin-applies/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        adminApplyService.approve(id);
        return Result.success("已通过", null);
    }

    @Operation(summary = "驳回申请", description = "超管驳回管理员申请")
    @PutMapping("/api/admin/admin-applies/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody AdminApply body) {
        adminApplyService.reject(id, body.getReply());
        return Result.success("已驳回", null);
    }
}
