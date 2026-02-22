package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "聊天消息实体")
@TableName(value = "chat_message", autoResultMap = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChatMessage implements Serializable {

    @Schema(description = "消息ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房间ID", example = "room123")
    private Long roomId;

    @Schema(description = "消息发送者UID", example = "自己的id")
    private String fromUid;

    @Schema(description = "消息内容", example = "Hello, how are you?")
    private String content;

    @Schema(description = "回复的消息ID", example = "2101299")
    private Long replyMsgId;

    @Schema(description = "消息状态 (0-正常, 1-删除)", example = "0")
    private Integer status;

    @Schema(description = "与回复的消息间隔多少条", example = "3")
    private Long gapCount;

    /**
     * @see MessageTypeEnum
     */
    @Schema(description = "消息类型", example = "1")
    private Integer type;

    @Schema(description = "扩展信息", example = "{ \"key\": \"value\" }")
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private ChatMessageExtra extra;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
