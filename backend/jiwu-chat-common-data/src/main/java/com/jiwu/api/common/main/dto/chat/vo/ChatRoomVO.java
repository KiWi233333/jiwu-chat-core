package com.jiwu.api.common.main.dto.chat.vo;

import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.enums.chat.ContactNoticeStatus;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 房间详情VO
 *
 * @className: ChatRoomVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/18 13:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomVO {
    @Schema(description = "房间id")
    private Long roomId;

    @Schema(description = "房间类型 1群聊 2单聊 3AI ...")
    /**
     * 房间类型 1群聊 2单聊 3AI ...
     *
     * @see RoomTypeEnum
     */
    private Integer type;

    @Schema(description = "是否全员展示的会话 0否 1是")
    private Integer hotFlag;

    @Schema(description = "最新消息")
    private String text;

    @Schema(description = "会话名称")
    private String name;

    @Schema(description = "会话头像")
    private String avatar;

    @Schema(description = "房间最后活跃时间(用来排序)")
    private Date activeTime;

    @Schema(description = "未读数")
    private Long unreadCount;

    @Schema(description = "详细信息")
    private ChatRoomGroupVO roomGroup;

    @Schema(description = "角色信息")
    private ChatGroupMember member;

    @Schema(description = "对方的用户id（单聊时有效）")
    private String targetUid;

    @Schema(description = "是否在房间中")
    private Integer selfExist;

    @Schema(description = "置顶时间")
    private Date pinTime;

    /**
     * 提醒状态码
     * @see ContactNoticeStatus
     */
    @Schema(description = "提醒状态码", example = "1-3")
    private Integer noticeStatus;

    @Schema(description = "免打扰状态", example = "0")
    private Integer shieldStatus;


    @Schema(description = "最后一条消息的id")
    private Long lastMsgId;

}
