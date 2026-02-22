package com.jiwu.api.common.main.vo.chat;

import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Description: 好友校验
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserFriendVO {

    @Schema(description = "好友uid")
    private String userId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickName;

    /**
     * 用户类型
     *
     * @see UserType
     */
    @Schema(description = "用户类型")
    private Integer type;

    /**
     * @see ChatActiveStatusEnum
     */
    @Schema(description = "在线状态 1在线 2离线")
    private Integer activeStatus;
}
