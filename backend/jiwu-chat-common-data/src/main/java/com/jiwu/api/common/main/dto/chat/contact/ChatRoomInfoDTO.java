package com.jiwu.api.common.main.dto.chat.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since 2023-03-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomInfoDTO {

    @Schema(description = "房间号id")
    private Long roomId;

    @Schema(description = "私聊id")
    private String userId;


}
