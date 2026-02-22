package com.jiwu.api.common.main.dto.chat.msg.body;

import com.jiwu.api.common.enums.FileMimeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Description: 语音消息入参
 * Date: 2023-06-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class FileMsgDTO extends BaseFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名（带后缀）")
    @NotBlank
    private String fileName;

    @Schema(description = "文件mimeType")
    private String mimeType;

    @Schema(description = "文件类型")
    private FileMimeTypeEnum fileType;

    @Schema(description = "回复的消息id")
    private Long replyMsgId;

    @Schema(description = "@ 列表")
    @Size(max = 10, message = "不能 @ 太多人啦！")
    private List<MentionInfo> mentionList;
}
