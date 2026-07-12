package com.blog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章详情（含正文，不含浏览量统计等管理字段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章详情")
public class ArticleDetailVO {

    @Schema(description = "文章 ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "正文")
    private String content;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "作者 ID")
    private Long userId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "状态：0-草稿 1-待审核 2-已发布 3-驳回")
    private Integer status;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "驳回原因（仅驳回状态时有值）")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
