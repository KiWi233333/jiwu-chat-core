package com.jiwu.api.chat.service.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * 聊天消息初始化服务（开源版无 AI 回复修复逻辑）
 */
@Service
@Slf4j
public class ChatMessageInitService implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("聊天模块初始化完成");
    }
}
