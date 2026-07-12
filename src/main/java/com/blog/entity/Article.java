package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 简介 */
    private String summary;

    /** 正文 */
    private String content;

    /** 封面图 */
    private String coverUrl;

    /** 分类 ID */
    private Long categoryId;

    /** 作者 ID */
    private Long userId;

    /**
     * 状态：
     * 0 - 草稿
     * 1 - 待审核
     * 2 - 已发布
     * 3 - 已驳回
     */
    private Integer status;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 驳回原因 */
    private String rejectReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
