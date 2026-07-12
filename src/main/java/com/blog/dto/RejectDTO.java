package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 驳回请求参数
 */
@Data
@Schema(description = "审核驳回请求")
public class RejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 255, message = "驳回原因最长 255 个字符")
    @Schema(description = "驳回原因", example = "内容涉及敏感话题，请修改后重新提交")
    private String reason;
}
