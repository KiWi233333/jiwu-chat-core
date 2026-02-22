package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户验证身份code
 *
 * @className: BillsTotalVO
 * @author: Kiwi23333
 * @description: 用户验证身份code
 * @date: 2023/7/18 20:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckCodeDTO {

    @Schema(description = "手机号/邮箱")
    @NotBlank(message = "手机号/邮箱不能为空")
    String key;

    @Schema(description = "标识（0手机号，1验证码）")
    @NotBlank(message = "关键标识不能为空")
    @Range(min = 0, max = 1, message = "错误，参数有误！")
    Integer type;

    @Schema(description = "验证码")
    @Length(min = 6, max = 6, message = "验证码长度为6个字符")
    @NotBlank(message = "验证码不能为空")
    String code;
}
