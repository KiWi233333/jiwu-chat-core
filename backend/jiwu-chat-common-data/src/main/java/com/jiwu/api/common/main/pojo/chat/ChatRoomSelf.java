package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "单聊房间表")
@TableName(value = "chat_room_self")
public class ChatRoomSelf implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * uid1（更小的uid）
     */
    @TableField("uid1")
    private String uid1;

    /**
     * uid2（更大的uid）
     */
    @TableField("uid2")
    private String uid2;

    /**
     * 房间key由两个uid拼接，先做排序uid1,uid2
     */
    @TableField("room_key")
    private String roomKey;

    /**
     * 房间状态 0禁用(删好友了禁用) 1正常
     */
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
