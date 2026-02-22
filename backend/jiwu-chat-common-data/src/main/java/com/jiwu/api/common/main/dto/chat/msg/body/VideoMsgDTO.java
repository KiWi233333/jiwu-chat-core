package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Description: 视频消息入参
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMsgDTO implements Serializable {
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

    @Schema(description = "回复的消息id")
    private Long replyMsgId;

    @Schema(description = "@ 列表")
    @Size(max = 10, message = "不能 @ 太多人啦！")
    private List<MentionInfo> mentionList;
}
