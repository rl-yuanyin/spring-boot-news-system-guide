package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Appeal;
import com.blog.service.AppealService;
import com.blog.service.NotificationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "申诉管理", description = "用户申诉提交与管理员审核")
@RestController
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final NotificationService notificationService;

    @Operation(summary = "提交申诉", description = "被封禁用户提交申诉（公开接口）")
    @PostMapping("/api/appeal")
    public Result<Void> submit(@RequestBody Appeal appeal) {
        appealService.submit(appeal.getUsername(), appeal.getReason());
        return Result.success("申诉已提交，请等待审核", null);
    }

    @Operation(summary = "查询申诉状态", description = "根据用户名查询申诉状态")
    @GetMapping("/api/appeal/{username}")
    public Result<Appeal> myAppeal(@PathVariable String username) {
        Appeal appeal = appealService.getByUsername(username);
        return Result.success(appeal);
    }

    @Operation(summary = "申诉列表", description = "管理员查看所有申诉")
    @GetMapping("/api/admin/appeals")
    public Result<Page<Appeal>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(appealService.listAppeals(page, pageSize));
    }

    @Operation(summary = "通过申诉", description = "管理员通过申诉，解封用户")
    @PutMapping("/api/admin/appeals/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        appealService.approve(id);
        return Result.success("申诉已通过", null);
    }

    @Operation(summary = "驳回申诉", description = "管理员驳回申诉")
    @PutMapping("/api/admin/appeals/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Appeal body) {
        appealService.reject(id, body.getReply());
        return Result.success("已驳回", null);
    }
}
