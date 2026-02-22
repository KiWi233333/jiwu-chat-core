package com.jiwu.api.common.main.dto.chat.vo;

import com.jiwu.api.common.main.dto.chat.msg.body.MentionInfo;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Description: 视频消息入参
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMsgVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "下载地址")
    @NotBlank
    private String url;

    @Schema(description = "大小（字节）")
    private Long size;

    @Schema(description = "视频时长（秒）")
    @NotNull(message = "视频时长不能为空")
    @Range(min = 1, max = 1800, message = "视频时长必须在1-1800秒之间")
    private Integer duration;

    @Schema(description = "缩略图下载地址")
    @NotBlank
    private String thumbUrl;

    @Schema(description = "缩略图宽度（像素）")
    private Integer thumbWidth;

    @Schema(description = "缩略图高度（像素）")
    private Integer thumbHeight;

    @Schema(description = "缩略图大小（字节）")
    private Long thumbSize;


    @Schema(description = "消息链接映射")
    private Map<String, UrlInfoDTO> urlContentMap;

    @Schema(description = "@ 列表")
    private List<MentionInfo> mentionList;

    @Schema(description = "父消息，如果没有父消息，返回的是null")
    private TextBodyMsgVO.ReplyMsg reply;
}
