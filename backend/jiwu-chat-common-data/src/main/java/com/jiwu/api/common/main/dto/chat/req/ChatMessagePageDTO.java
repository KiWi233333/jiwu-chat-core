package com.jiwu.api.common.main.dto.chat.req;

import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * Description: 消息列表请求
 * Date: 2023-03-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessagePageDTO extends CursorPageBaseDTO {
    @NotNull
    @Schema(description = "会话id")
    private Long roomId;
}
