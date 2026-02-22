package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "聊天会话实体")
@TableName(value = "chat_contact")
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class ChatContact implements Serializable {
    private static final long serialVersionUID = 1L;


    @Schema(description = "会话id", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID", example = "user123")
    private String userId;

    @Schema(description = "房间ID", example = "room123")
    private Long roomId;

    @Schema(description = "会话内消息最后更新的时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date activeTime;

    @Schema(description = "会话内消息最后阅读的时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date readTime;

    @Schema(description = "置顶时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date pinTime;

    @Schema(description = "提醒状态码", example = "0")
    private Integer noticeStatus;

    @Schema(description = "免打扰状态码", example = "0")
    private Integer shieldStatus;

    @Schema(description = "阅读到的消息ID", example = "2101299")
    private Long readMsgId;

    @Schema(description = "会话最新消息ID", example = "2101300")
    private Long lastMsgId;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
