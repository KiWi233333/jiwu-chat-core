package com.jiwu.api.common.main.dto.chat.vo;

import com.jiwu.api.common.enums.FileMimeTypeEnum;
import com.jiwu.api.common.main.dto.chat.msg.body.BaseFileDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.MentionInfo;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Description: 图片消息
 * Date: 2023-06-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class FileBodyMsgVO extends BaseFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名（带后缀）")
    @NotBlank
    private String fileName;

    @Schema(description = "文件mimeType")
    private String mimeType;

    @Schema(description = "文件类型")
    private FileMimeTypeEnum fileType;

    @Schema(description = "消息链接映射")
    private Map<String, UrlInfoDTO> urlContentMap;

    @Schema(description = "@ 列表")
    private List<MentionInfo> mentionList;

    @Schema(description = "父消息，如果没有父消息，返回的是null")
    private TextBodyMsgVO.ReplyMsg reply;
}


