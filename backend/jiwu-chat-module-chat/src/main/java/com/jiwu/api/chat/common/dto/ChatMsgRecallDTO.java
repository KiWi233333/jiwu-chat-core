package com.jiwu.api.chat.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Description:消息撤回的推送类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChatMsgRecallDTO {
    @Schema(description = "消息id")
    private Long msgId;
    @Schema(description = "房间id")
    private Long roomId;
    @Schema(description = "撤回的用户id")
    private String recallUid;
}
