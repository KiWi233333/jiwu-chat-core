package com.jiwu.api.common.util.service.translation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationToolVO {

    @Schema(description = "翻译工具名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String label;

    @Schema(description = "翻译工具值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer value;


}
