package com.jiwu.api.chat.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读申请
 *
 * @className: ChatUserFriendUnreadVO
 * @author: Kiwi23333
 * @description: 未读申请
 * @date: 2023/12/26 19:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserFriendUnReadVO {

    @Schema(description = "申请列表的未读数")
    private Long unReadCount;

}
