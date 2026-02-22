package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jiwu.api.common.main.enums.chat.HotFlagEnum;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Schema(description = "聊天房间实体")
@TableName(value = "chat_room", autoResultMap = true)
public class ChatRoom implements Serializable {

    @Schema(description = "房间ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房间类型（1-群聊，2-单聊）", example = "1")
    private Integer type;

    @Schema(description = "是否全员展示（0-否，1-是）", example = "0")
    private Integer hotFlag;

    @Schema(description = "会话中的最后一条消息ID")
    private Long lastMsgId;

    @Schema(description = "额外信息（根据不同类型房间有不同存储的东西）")
    private String extJson;

    @Schema(description = "群最后消息的更新时间（热点群不需要写扩散，只更新这里）", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;



    @JsonIgnore
    public boolean isHotRoom() {
        return HotFlagEnum.of(this.hotFlag) == HotFlagEnum.YES;
    }
    @JsonIgnore
    public boolean isAiRoom() {
        return RoomTypeEnum.of(this.type) == RoomTypeEnum.AI;
    }

    @JsonIgnore
    public boolean isRoomFriend() {
        return RoomTypeEnum.of(this.type) == RoomTypeEnum.FRIEND;
    }
    @JsonIgnore
    public boolean isRoomAI() {
        return RoomTypeEnum.of(this.type) == RoomTypeEnum.AI;
    }

    @JsonIgnore
    public boolean isRoomGroup() {
        return RoomTypeEnum.of(this.type) == RoomTypeEnum.GROUP;
    }
}
