package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Description: 文本消息入参
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextMsgDTO {

    @Schema(description = "回复的消息id,如果没有别传就好")
    private Long replyMsgId;

    @Schema(description = "艾特的uid")
    @Size(max = 10, message = "不能 @ 太多人啦！")
    private List<String> atUidList;

    @Schema(description = "@ 列表")
    @Size(max = 10, message = "不能 @ 太多人啦！")
    private List<MentionInfo> mentionList;

}
