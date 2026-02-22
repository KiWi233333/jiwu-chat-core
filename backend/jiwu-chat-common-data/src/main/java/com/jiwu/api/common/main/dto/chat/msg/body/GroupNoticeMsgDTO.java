package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 群通知消息DTO
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupNoticeMsgDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "回复消息ID")
    private Long replyMsgId;

    @Schema(description = "是否群发")
    @Range(min = 0, max = 1, message = "是否群发参数错误！")
    private Integer noticeAll;

    // 消费图片列表
    @Schema(description = "消费图片列表")
    @Size(max = 10, message = "使用图片数量不能超过10张！")
    private List<String> imgList;

}
