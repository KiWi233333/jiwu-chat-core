package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "用户联系人实体")
@TableName(value = "chat_user_friend", autoResultMap = true)
public class ChatUserFriend implements Serializable {

    @Schema(description = "ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户UID", example = "用户的uid")
    private String userId;

    @Schema(description = "好友UID", example = "好友的uid")
    private String friendUid;

    @Schema(description = "逻辑删除（0-正常，1-删除）", example = "0")
    private Integer deleteStatus;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
