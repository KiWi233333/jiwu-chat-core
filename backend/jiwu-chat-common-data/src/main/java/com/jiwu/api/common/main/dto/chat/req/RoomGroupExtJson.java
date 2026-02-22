package com.jiwu.api.common.main.dto.chat.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jiwu.api.common.main.enums.chat.InvitePermissionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 群聊详情
 *
 * @className: RoomGroupExtJson
 * @author: Kiwi23333
 * @description: 群聊扩展 JSON，用于群聊属性扩展
 * @date: 2024/06/26 12:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomGroupExtJson {

    @Schema(description = "群聊公告")
    @Length(max = 200, message = "群公告长度不能超过200字符！")
    private String notice;

    @Schema(description = "邀请权限 0-任意成员可邀请 1-管理员和群主可邀请 2-仅群主可邀请")
    @JsonProperty("invite_permission")
    private InvitePermissionEnum invitePermission;

}
