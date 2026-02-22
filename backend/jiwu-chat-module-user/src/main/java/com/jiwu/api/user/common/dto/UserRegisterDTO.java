package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.annotation.Phone;
import com.jiwu.api.user.common.enums.UserRegisterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.*;

/**
 * 注册字段类
 *
 * @className: UserRegister
 * @author: Kiwi23333
 * @description: 注册的字段
 * @date: 2023/4/21 23:49
 */
@Data
public class UserRegisterDTO {
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 6, max = 20, message = "用户名长度在6-20个字符之间")
    @NotBlank(message = "用户名不能为空")
    String username;


    @Schema(description = "手机号")
    @Phone
    String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱不合法")
    String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    String password;

    @Schema(description = "注册类型", example = "0 手机号，1 邮箱")
    @NotNull(message = "注册类型不能为空")
    @Range(min = 0, max = 1, message = "注册类型错误！")
    Integer type;


    @Schema(description = "手机|邮箱 验证码")
    @NotBlank(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码为6位组成")
    String code;


    // 判断是否 是 0 手机号，1 邮箱
    public boolean isPhone() {
        return UserRegisterType.PHONE.getType().equals(type);
    }

    public boolean isEmail() {
        return UserRegisterType.EMAIL.getType().equals(type);
    }

}
