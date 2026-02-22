package com.jiwu.api.common.main.dto.chat.reaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息表情反应 Toggle 请求DTO
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息表情反应请求")
public class ReactionToggleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "消息ID不能为空")
    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long msgId;

    @NotBlank(message = "emoji编码不能为空")
    @Schema(description = "emoji编码（如 thumbs_up、heart 等）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String emojiType;
}
