package com.jiwu.api.chat.common.vo.ws;

import com.jiwu.api.common.main.pojo.chat.ChatUserApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友申请人
 * Date: 2023-03-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSFriendApply {

    @Schema(description = "申请人")
    private String uid;

    @Schema(description = "申请明细")
    private ChatUserApply apply;

    @Schema(description = "申请未读数")
    private Long unreadCount;
}
