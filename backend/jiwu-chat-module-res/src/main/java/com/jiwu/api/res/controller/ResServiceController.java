package com.jiwu.api.res.controller;

import com.jiwu.api.res.service.ResService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 资源模块
 */
@Slf4j
@Tag(name = "资源模块/AI客服")
@RestController
@RequestMapping("/res/service")
public class ResServiceController {

    @Resource
    private ResService resService;

// TODO: 待修改
//    @Operation(summary = "ai客服助手对话(sse)")
//    @PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @PortFlowControl(limit = 10, time = 5, timeUnit = TimeUnit.MINUTES, errorMessage = "ai客服助手对话过于频繁（10分钟/10次！")
//    SseEmitterUTF8 systemService(@RequestHeader(name = HEADER_NAME) String token, @Valid @RequestBody AiBaseMessage dto) {
//        return resService.systemServiceChat(dto);
//    }

}
