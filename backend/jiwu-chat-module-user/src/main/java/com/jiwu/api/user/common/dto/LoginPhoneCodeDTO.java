package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
public class LoginPhoneCodeDTO {

    @Schema(description = "手机号")
    @Phone
    @NotBlank(message = "手机号不能为空")
    String phone;


    @Schema(description = "验证码")
    @Length(min = 6,max = 6,message = "验证码长度为6个字符")
    @NotBlank(message = "验证码不能为空")
    String code;

}
