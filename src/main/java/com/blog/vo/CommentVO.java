package com.blog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论视图")
public class CommentVO {

    @Schema(description = "评论 ID")
    private Long id;

    @Schema(description = "文章 ID")
    private Long articleId;

    @Schema(description = "评论用户 ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String username;

    @Schema(description = "评论用户头像")
    private String avatar;

    @Schema(description = "父评论 ID")
    private Long parentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
