package com.jiwu.api.res.common.dto;

import com.jiwu.api.common.main.enums.res.TranslationLangEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranslationDTO {
    @Length(min = 1, max = 2000, message = "翻译内容长度在1-2000个字符之间！")
    @NotBlank(message = "翻译内容不能为空！")
    private String text;

    @NotNull(message = "原语言代码不能为空！")
    private TranslationLangEnums sourceLang;

    @NotNull(message = "目标语言代码不能为空！")
    private TranslationLangEnums targetLang;

    // 翻译类型 1 腾讯 2 ai
    @Range(min = 1, max = 2, message = "翻译类型错误！")
    @NotNull(message = "翻译类型不能为空！")
    private Integer type = 1;

}
