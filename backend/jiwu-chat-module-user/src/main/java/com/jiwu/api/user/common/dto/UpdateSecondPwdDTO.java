package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

/**
 * @className: UpdatePwdDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/9 2:30
 */
@Data
public class UpdateSecondPwdDTO {

    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空!")
    @Length(min = 6, max = 6, message = "验证码长度为6位!")
    String code;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空!")
    @Password
    String newPassword;

}
