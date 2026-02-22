package com.jiwu.api.chat.common.vo;

import com.jiwu.api.common.main.enums.user.Gender;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;

/**
 * Description: 消息
 * Date: 2023-03-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {

    @Schema(description = "发送者信息")
    private UserInfo fromUser;
    @Schema(description = "消息详情")
    private Message message;

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class UserInfo {
        @Schema(description = "用户id")
        private String userId;
        @Schema(description = "昵称")
        private String nickName;
        @Schema(description = "性别 （男|女|保密）")
        private Gender gender;
        @Schema(description = "头像icon")
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Message {
        @Schema(description = "消息id")
        private Long id;
        @Schema(description = "房间id")
        private Long roomId;
        @Schema(description = "文本内容")
        private String content;
        @Schema(description = "消息发送时间")
        private Date sendTime;
        /**
         * 消息类型
         * @see MessageTypeEnum
         */
        @Schema(description = "消息类型")
        private Integer type;
        @Schema(description = "消息内容不同的消息类型，内容体不同")
        private Object body;
        @Schema(description = "消息表情反应聚合列表")
        private List<ReactionVO> reactions;
    }

    @Schema(description = "消息客户端id标识（不保存）")
    @Length(max = 100, message = "客户端id标识不能超过100个字符！")
    private String clientId;
}
