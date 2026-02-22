package com.jiwu.api.common.main.constant.chat;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMqConstant {
    public static final String DIRECT_CHAT_EXCHANGE = "direct_chat_exchange";

    /******************************* 发送队列 ****************************/
    public static final String DIRECT_SEND_CHAT_QUEUE = "direct_group_chat_queue";
    public static final String DIRECT_SEND_CHAT_KEY = "direct_group_chat_key";

    /******************************* 推送队列 ****************************/
    public static final String DIRECT_PUSH_CHAT_QUEUE = "direct_push_chat_queue";
    public static final String DIRECT_PUSH_CHAT_KEY = "direct_push_chat_key";
}
