package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息表情反应实体
 *
 * @author Kiwi23333
 * @description 消息emoji反应
 * @date 2026/02/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "消息表情反应实体")
@TableName("chat_message_reaction")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChatMessageReaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "消息ID")
    private Long msgId;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "emoji编码")
    private String emojiType;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
