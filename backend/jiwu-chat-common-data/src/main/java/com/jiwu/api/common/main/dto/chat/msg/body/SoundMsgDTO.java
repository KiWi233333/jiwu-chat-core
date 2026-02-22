package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Description: 语音消息入参
 * Date: 2023-06-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoundMsgDTO extends BaseFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "语音消息长度，单位：s")
    @Range(min = 1, max = 120, message = "语音只长度1-120秒！")
    @NotNull(message = "语音时长不能为空！")
    private Long second;

    @Schema(description = "转文本")
    @Length(min = 0, max = 500, message = "语音转文本长度0-500！")
    private String translation;
}


