package com.jiwu.api.common.main.dto.chat.msg;

import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;


/**
 * 聊天信息点播
 * Description: 消息发送请求体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChatMessageDTO {

    @Schema(description = "房间id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "房间不能为空！")
    private Long roomId;


    @Schema(description = "消息客户端id标识（不保存）")
    @Length(max = 100, message = "客户端id标识不能超过100个字符！")
    private String clientId;

    /**
     * @see MessageTypeEnum
     */
    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "消息类型不能为空！")
    @Range(min = 1, message = "消息类型错误！")
//    @Pattern(regexp = "^(1|2|3|4|5|8|10)$", message = "消息类型错误！") // 6 7 9 11 暂时不支持
    private Integer msgType;

    @Schema(description = "文本消息（可选）")
//    @Length(max = 512, message = "文本不能超过512个字符！")
    private String content = "";

    @Schema(description = "消息内容，类型不同传值不同")
    @NotNull(message = "消息扩展不能为空！")
    private Object body;


}
