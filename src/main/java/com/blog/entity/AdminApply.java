package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_apply")
public class AdminApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reason;
    private Integer articleCount;
    private Long totalViews;
    /** 0-待审核 1-已通过 2-已拒绝 */
    private Integer status;
    private String reply;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
