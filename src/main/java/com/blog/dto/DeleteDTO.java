package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "管理员删除文章请求")
public class DeleteDTO {

    @NotBlank(message = "删除原因不能为空")
    @Schema(description = "删除原因", example = "不实信息")
    private String reason;
}
