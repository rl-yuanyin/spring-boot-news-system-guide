package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类请求参数
 */
@Data
@Schema(description = "分类请求")
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最长 50 个字符")
    @Schema(description = "分类名称", example = "科技")
    private String name;

    @Schema(description = "排序（越小越靠前）", example = "1")
    private Integer sort;
}
