package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论请求参数
 */
@Data
@Schema(description = "评论请求")
public class CommentDTO {

    @NotNull(message = "文章 ID 不能为空")
    @Schema(description = "文章 ID", example = "1")
    private Long articleId;

    @Schema(description = "父评论 ID（回复评论时传，可选）")
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容最长 500 个字符")
    @Schema(description = "评论内容", example = "写得很好，学习了！")
    private String content;
}
