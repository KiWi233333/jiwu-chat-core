package com.jiwu.api.common.main.dto.chat.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Description:
 * Date: 2023-08-12
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChatMsgMqDTO implements Serializable {
    private Long msgId;
    private String clientId;
}
