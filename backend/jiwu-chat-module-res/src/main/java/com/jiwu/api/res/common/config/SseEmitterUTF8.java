package com.jiwu.api.res.common.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SseEmitterUTF8 extends SseEmitter {
    @Override
    protected void extendResponse(@NotNull ServerHttpResponse outputMessage) {
        super.extendResponse(outputMessage);
        HttpHeaders headers = outputMessage.getHeaders();
        headers.setContentType( new MediaType("text", "event-stream", StandardCharsets.UTF_8));
    }
    public SseEmitterUTF8(Long timeout) {
        super(timeout);
    }
}
