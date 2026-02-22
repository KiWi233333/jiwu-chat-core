package com.jiwu.api.chat.common.vo.room;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jiwu.api.common.main.dto.chat.vo.RoomGroupExtJsonVO;
import com.jiwu.api.common.main.enums.chat.GroupRoleAPPEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群聊信息VO
 *
 * @className: ChatRoomGroupInfoVO
 * @author: Kiwi23333
 * @description: 群聊信息VO
 * @date: 2023/12/26 16:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomInfoVO {
    @Schema(description = "房间id")
    private Long roomId;
    @Schema(description = "群名称")
    private String groupName;
    @Schema(description = "群头像")
    private String avatar;
    @Schema(description = "在线人数")
    private Long onlineNum;//在线人数
    @Schema(description = "总人数")
    private Long allUserNum;
    @Schema(description = "是否全员展示（0-否，1-是）", example = "0")
    private Integer hotFlag;
    /**
     * @see GroupRoleAPPEnum
     */
    @Schema(description = "成员角色 1群主 2管理员 3普通成员")
    private Integer role;

    @Schema(description = "额外信息（根据不同类型房间有不同存储的东西）")
    private RoomGroupExtJsonVO detail;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
