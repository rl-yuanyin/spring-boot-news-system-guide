package com.blog.controller;

import com.blog.common.Constants;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleQueryDTO;
import com.blog.dto.DeleteDTO;
import com.blog.dto.RejectDTO;
import com.blog.entity.Article;
import com.blog.exception.BusinessException;
import com.blog.service.ArticleService;
import com.blog.service.NotificationService;
import com.blog.utils.JwtUtils;
import com.blog.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理员控制器（文章审核、用户管理、数据统计等）
 */
@Tag(name = "管理端", description = "文章审核、用户管理等管理员专属操作")
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ArticleService articleService;
    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;

    // ========================================
    // 文章审核
    // ========================================

    @Operation(summary = "查询待审核文章", description = "管理员查看所有待审核的文章列表，按提交时间升序")
    @GetMapping("/articles/pending")
    public Result<PageResult<ArticleVO>> pending(ArticleQueryDTO dto) {
        checkAdmin();
        return Result.success(articleService.pagePending(dto));
    }

    @Operation(summary = "审核通过", description = "管理员通过文章审核，文章状态变为已发布")
    @PostMapping("/articles/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        checkAdmin();
        articleService.approve(id);
        return Result.success("审核通过", null);
    }

    @Operation(summary = "审核驳回", description = "管理员驳回文章，需填写驳回原因，作者可查看")
    @PostMapping("/articles/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectDTO dto) {
        checkAdmin();
        articleService.reject(id, dto.getReason());
        return Result.success("已驳回", null);
    }

    @Operation(summary = "管理员删除文章", description = "管理员删除违规文章，需选择删除原因，系统自动通知作者")
    @PostMapping("/articles/{id}/delete")
    public Result<Void> deleteArticle(@PathVariable Long id, @Valid @RequestBody DeleteDTO dto) {
        Long userId = checkAndGetAdminId();
        Article article = articleService.getById(id);
        if (article == null) throw BusinessException.notFound("文章不存在");

        articleService.delete(id, userId);
        notificationService.send(article.getUserId(), "admin_delete",
                "你的文章《" + article.getTitle() + "》因「" + dto.getReason() + "」被管理员下架", id);
        return Result.success("已删除并通知作者", null);
    }

    // ========================================
    // 数据导出
    // ========================================

    @Operation(summary = "导出文章 Excel", description = "管理员导出文章数据为 Excel 文件，支持按时间范围筛选")
    @GetMapping("/articles/export")
    public void export(HttpServletResponse response) throws IOException {
        checkAdmin();

        // 查询所有已发布文章
        ArticleQueryDTO dto = new ArticleQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10000); // 一次最多导出 10000 条
        List<ArticleVO> articles = articleService.pageQuery(dto, null).getList();

        // 创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("文章数据");

        // 表头
        String[] headers = {"ID", "标题", "作者", "分类", "状态", "浏览量", "点赞数", "是否置顶", "创建时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // 状态映射
        String[] statusNames = {"草稿", "待审核", "已发布", "已驳回"};

        // 数据行
        int rowIndex = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ArticleVO a : articles) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(a.getId());
            row.createCell(1).setCellValue(a.getTitle());
            row.createCell(2).setCellValue(a.getAuthorName());
            row.createCell(3).setCellValue(a.getCategoryName());
            int statusIdx = a.getStatus() != null && a.getStatus() >= 0 && a.getStatus() < statusNames.length
                    ? a.getStatus() : 0;
            row.createCell(4).setCellValue(statusNames[statusIdx]);
            row.createCell(5).setCellValue(a.getViewCount() != null ? a.getViewCount() : 0);
            row.createCell(6).setCellValue(a.getLikeCount() != null ? a.getLikeCount() : 0);
            row.createCell(7).setCellValue(a.getIsTop() != null && a.getIsTop() == 1 ? "是" : "否");
            row.createCell(8).setCellValue(a.getCreateTime() != null ? a.getCreateTime().format(formatter) : "");
        }

        // 设置列宽
        sheet.setColumnWidth(0, 8 * 256);
        sheet.setColumnWidth(1, 40 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        sheet.setColumnWidth(4, 10 * 256);
        sheet.setColumnWidth(5, 10 * 256);
        sheet.setColumnWidth(6, 10 * 256);
        sheet.setColumnWidth(7, 10 * 256);
        sheet.setColumnWidth(8, 20 * 256);

        // 设置响应头，触发浏览器下载
        String fileName = "文章数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ========================================
    // 权限辅助
    // ========================================

    /**
     * 校验管理员权限，返回管理员 ID
     */
    private Long checkAndGetAdminId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw BusinessException.unauthorized("请先登录");
        String authHeader = attrs.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw BusinessException.unauthorized("请先登录");
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token))
            throw BusinessException.unauthorized("token 无效或已过期");
        if (jwtUtils.getRole(token) != 1)
            throw BusinessException.forbidden("仅管理员可操作");
        return jwtUtils.getUserId(token);
    }

    /**
     * 校验管理员权限
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
