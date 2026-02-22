package com.jiwu.api.common.main.dto.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息已读未读VO
 *
 * @className: ChatMessageReadVO
 * @author: Kiwi23333
 * @description: 消息已读未读VO
 * @date: 2023/12/26 18:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageReadVO {
    @Schema(description = "已读或者未读的用户uid")
    private String uid;
}
