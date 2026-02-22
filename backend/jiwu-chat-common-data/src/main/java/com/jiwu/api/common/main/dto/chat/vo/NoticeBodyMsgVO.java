package com.jiwu.api.common.main.dto.chat.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本消息返回体
 * Date: 2023-06-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoticeBodyMsgVO {
    @Schema(description = "是否群发消息")
    private Integer noticeAll;

    @Schema(description = "父消息，如果没有父消息，返回的是null")
    private ReplyMsg reply;

    @Schema(description = "图片列表")
    private List<String> imgList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyMsg {
        @Schema(description = "消息id")
        private Long id;
        @Schema(description = "用户uid")
        private String uid;
        @Schema(description = "用户名称")
        private String nickName;
        @Schema(description = "消息类型 1正常文本 2.撤回消息")
        private Integer type;
        @Schema(description = "消息内容不同的消息类型，见父消息内容体")
        private Object body;
        @Schema(description = "是否可消息跳转 0否 1是")
        private Integer canCallback;
        @Schema(description = "跳转间隔的消息条数")
        private Long gapCount;
    }
}
