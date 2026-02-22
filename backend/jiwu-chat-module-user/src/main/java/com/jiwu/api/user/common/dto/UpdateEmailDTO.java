package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 修改用户邮箱参数类
 *
 * @className: UpdatePhoneDTO
 * @author: Kiwi23333
 * @description: 修改用户邮箱参数类
 * @date: 2023/5/10 16:29
 */
@Data
public class UpdateEmailDTO {


    @Schema(description = "新邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email
    @NotBlank(message = "新邮箱不能为空！")
    private String newEmail;

    @Schema(description = "新邮箱-验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
