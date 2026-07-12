package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文章创建/更新请求参数
 */
@Data
@Schema(description = "文章创建/更新请求")
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 个字符")
    @Schema(description = "标题", example = "Spring Boot 入门教程")
    private String title;

    @Size(max = 200, message = "摘要最长 200 个字符")
    @Schema(description = "简介", example = "这是一篇 Spring Boot 入门教程")
    private String summary;

    @Schema(description = "正文（富文本 HTML）", example = "<p>Spring Boot 是由 Pivotal 团队...</p>")
    private String content;

    @Schema(description = "封面图 URL", example = "/uploads/cover.jpg")
    private String coverUrl;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "是否保存为草稿（true=草稿，false/不传=提交审核）", example = "false")
    private Boolean draft;
}
