package com.jiwu.api.common.main.dto.chat.msg.body;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Description: 图片消息入参
 * Date: 2023-06-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ImgMsgDTO extends BaseFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "宽度（像素）")
    @NotNull(message = "上传图片宽度不能为空！")
    @Range(min = 0, message = "上传图片宽度不能为空！")
    private Integer width;

    @Schema(description = "高度（像素）")
    @NotNull(message = "上传图片高度不能为空！")
    @Range(min = 0, message = "上传图片高度不能为空！")
    private Integer height;

    @Schema(description = "回复的消息id")
    private Long replyMsgId;

    @Schema(description = "@ 列表")
    @Size(max = 10, message = "不能 @ 太多人啦！")
    private List<MentionInfo> mentionList;

}


