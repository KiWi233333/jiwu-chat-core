package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "用户申请实体")
@TableName(value = "chat_user_apply", autoResultMap = true)
public class ChatUserApply implements Serializable {

    @Schema(description = "申请ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "申请人UID", example = "申请人的uid")
    private String userId;

    @Schema(description = "接收人UID", example = "接收人的uid")
    private String targetId;

    @Schema(description = "申请类型（1-加好友）", example = "1")
    private Integer type;

    @Schema(description = "申请信息", example = "申请加好友的信息")
    private String msg;

    @Schema(description = "申请状态（0-待审批，1-同意）", example = "1")
    private Integer status;

    @Schema(description = "阅读状态（0-未读，1-已读）", example = "0")
    private Integer readStatus;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
