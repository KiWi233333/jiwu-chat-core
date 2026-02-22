package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 描述
 *
 * @className: LoginDto
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/4/21 15:23
 */
@Data
public class LoginDTO {
    @Schema(description = "用户名/手机号/邮箱")
    @NotBlank(message = "用户名/手机号/邮箱不能为空")
    String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Password
    String password;

}
