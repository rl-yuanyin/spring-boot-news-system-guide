package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文章查询请求参数
 */
@Data
@Schema(description = "文章查询请求")
public class ArticleQueryDTO {

    @Schema(description = "关键词搜索（标题+作者名）", example = "Spring")
    private String keyword;

    @Schema(description = "分类 ID 筛选", example = "1")
    private Long categoryId;

    @Schema(description = "状态筛选：0-草稿 1-待审核 2-已发布 3-驳回（不传则默认查已发布）")
    private Integer status;

    @Schema(description = "是否置顶筛选")
    private Integer isTop;

    @Schema(description = "当前页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
