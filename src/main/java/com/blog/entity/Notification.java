package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者用户 ID */
    private Long userId;

    /** 通知类型：like / comment / admin_delete */
    private String type;

    /** 通知内容 */
    private String content;

    /** 关联文章 ID */
    private Long articleId;

    /** 0-未读 1-已读 */
    private Integer isRead;

    /** 通知时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
