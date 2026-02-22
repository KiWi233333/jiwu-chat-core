package com.jiwu.api.common.main.dto.chat.vo;

import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
@Accessors(chain = true)
public class ChatRoomGroupVO {

    @Schema(description = "id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "房间Id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomId;

    @Schema(description = "群名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "群头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatar;

    @Schema(description = "群状态(0-正常,1-删除)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer deleteStatus;

    @Schema(description = "修改时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "额外信息（根据不同类型房间有不同存储的东西）")
    private RoomGroupExtJsonVO detail;

    /**
     * 成员角色1群主(可撤回，可移除，可解散，可发系统通知) 2管理员(可撤回，可移除) 3普通成员
     */
    @Schema(description = "成员角色")
    private Integer role;

    @Schema(description = "加入群聊的时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinTime;

    @Schema(description = "成员更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date memberUpdateTime;
}
