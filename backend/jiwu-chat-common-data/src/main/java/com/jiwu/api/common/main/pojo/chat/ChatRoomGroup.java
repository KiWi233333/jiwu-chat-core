package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jiwu.api.common.main.dto.chat.req.RoomGroupExtJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "群聊房间实体")
@TableName(value = "chat_room_group", autoResultMap = true)
public class ChatRoomGroup implements Serializable {

    @Schema(description = "id", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房间Id", example = "1")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "群名称")
    private String name;

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "额外信息（根据不同类型房间有不同存储的东西）")
    @TableField(value = "ext_json", typeHandler = JacksonTypeHandler.class)
    private RoomGroupExtJson extJson;

    @Schema(description = "逻辑删除(0-正常,1-删除)", example = "0")
    @TableField("delete_status")
    private Integer deleteStatus;

    @Schema(description = "修改时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
