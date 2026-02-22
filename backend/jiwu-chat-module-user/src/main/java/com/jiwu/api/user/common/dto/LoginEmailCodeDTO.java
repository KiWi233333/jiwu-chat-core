package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 邮箱登录 入参类
 *
 * @className: LoginEmailCodeDTO
 * @author: Kiwi23333
 * @description: 邮箱登录
 * @date: 2023/4/21 15:23
 */
@Data
public class LoginEmailCodeDTO {

    @Schema(description = "邮箱")
    @Email
    @NotBlank(message = "邮箱不能为空")
    String email;


    @Schema(description = "验证码")
    @Length(min = 6,max = 6,message = "验证码长度为6个字符")
    @NotBlank(message = "验证码不能为空")
    String code;

}
