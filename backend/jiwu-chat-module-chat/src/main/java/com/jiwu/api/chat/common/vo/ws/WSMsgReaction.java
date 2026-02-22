package com.jiwu.api.chat.common.vo.ws;

import com.jiwu.api.chat.common.vo.ReactionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WebSocket 消息表情反应推送VO
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息表情反应推送")
public class WSMsgReaction {

    @Schema(description = "消息ID")
    private Long msgId;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "变更的emoji编码")
    private String emojiType;

    @Schema(description = "操作用户ID")
    private String userId;

    @Schema(description = "操作类型：1=添加, 0=取消")
    private Integer action;

    @Schema(description = "该消息最新的完整reaction聚合")
    private List<ReactionVO> reactions;
}
