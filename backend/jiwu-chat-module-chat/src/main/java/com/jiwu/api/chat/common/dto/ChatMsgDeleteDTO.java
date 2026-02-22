package com.jiwu.api.chat.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Description:消息删除的推送类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChatMsgDeleteDTO {
    @Schema(description = "消息id")
    private Long msgId;
    @Schema(description = "房间id")
    private Long roomId;
    @Schema(description = "删除的用户id")
    private String deleteUid;
}
