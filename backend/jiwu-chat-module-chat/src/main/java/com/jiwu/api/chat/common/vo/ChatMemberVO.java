package com.jiwu.api.chat.common.vo;

import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.pojo.sys.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Description: 群成员列表的成员信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberVO {

    @Schema(description = "userId")
    private String userId;

    private String username;

    private String nickName;

    private String avatar;
    /**
     * @see ChatActiveStatusEnum
     */
    @Schema(description = "在线状态 1在线 0离线")
    private Integer activeStatus;
    /**
     * 角色ID
     */
    private Integer roleType;

    @Schema(description = "最后一次上下线时间")
    private Date lastOptTime;

    public static ChatMemberVO build(User user) {
        return ChatMemberVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickName(user.getNickname())
                .avatar(user.getAvatar())
                .activeStatus(user.getActiveStatus())
                .lastOptTime(user.getLastLoginTime()).build();
    }
}
