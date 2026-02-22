package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改用户手机参数类
 *
 * @className: UpdatePhoneDTO
 * @author: Kiwi23333
 * @description: 修改用户手机参数类
 * @date: 2023/5/10 16:29
 */
@Data
public class UpdatePhoneDTO {


    @Schema(description = "新手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @Phone(message = "新手机号格式错误！")
    @NotBlank(message = "新手机号不能为空！")
    private String newPhone;

    @Schema(description = "新手机-验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 6,max = 6,message = "验证码必须为6位！")
    private String code;
}
