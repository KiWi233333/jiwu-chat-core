package com.jiwu.api.chat.common.vo.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 申请好友确认信息
 *
 * @className: FriendCheckVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/26 19:40
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserFriendCheckVO {

    @Schema(description = "校验结果")
    private List<FriendCheck> checkedList;

    @Data
    public static class FriendCheck {
        private String uid;
        private Integer isFriend;
    }

}
