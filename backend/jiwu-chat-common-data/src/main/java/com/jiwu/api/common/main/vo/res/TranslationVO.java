package com.jiwu.api.common.main.vo.res;

import com.jiwu.api.common.util.service.translation.TranslationToolVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述
 *
 * @className: FileOssVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/8/14 13:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationVO {

    @Schema(description = "翻译结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private String result;

    @Schema(description = "源语言")
    private String sourceLang;

    @Schema(description = "目标语言")
    private String targetLang;

    @Schema(description = "翻译工具")
    private TranslationToolVO tool;
}
