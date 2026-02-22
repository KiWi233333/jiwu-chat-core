package com.jiwu.api.common.main.dto.chat.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Description: 消息列表请求
 * Date: 2023-03-29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseChatMessageDTO {

    @NotNull
    @Schema(description = "消息id")
    private Long id;

    @NotNull
    @Schema(description = "房间id")
    private Long roomId;
}
