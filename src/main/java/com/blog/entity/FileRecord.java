package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_record")
public class FileRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原文件名 */
    private String originalName;

    /** 存储文件名（UUID 重命名） */
    private String fileName;

    /** 文件访问 URL */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件 MIME 类型 */
    private String fileType;

    /** 上传者 ID */
    private Long userId;

    /** 上传时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
