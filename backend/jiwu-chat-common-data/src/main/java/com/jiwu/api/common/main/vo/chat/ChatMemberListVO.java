package com.jiwu.api.common.main.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户群聊成员返回
 *
 * @className: ChatMemberListVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/26 12:07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberListVO {

    @Schema(description = "userId")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色")
    private Integer role;

}
