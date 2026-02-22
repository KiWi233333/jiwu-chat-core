package com.jiwu.api.res.controller;

import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.util.service.translation.TranslationUtil;
import com.jiwu.api.res.common.config.SseEmitterUTF8;
import com.jiwu.api.res.common.dto.TranslationDTO;
import com.jiwu.api.common.util.service.translation.TranslationToolVO;
import com.jiwu.api.common.main.vo.res.TranslationVO;
import com.jiwu.api.res.service.ResService;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 资源模块
 */
@Slf4j
@Tag(name = "资源模块/工具模块/翻译工具")
@RestController
@RequestMapping("/res/utils/translation")
public class ResTranslationController {

    @Resource
    private ResService resService;

    @Operation(summary = "翻译工具列表")
    @GetMapping("/list")
    Result<List<TranslationToolVO>> getTranslationToolList(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(TranslationUtil.TranslationToolEnums.getTools());
    }

    @Operation(summary = "翻译文本")
    @PostMapping("")
    @ReqPermission(name = "翻译文本", intro = "用户消费者翻译文本", expression = "res:utils:translation")
    @PortFlowControl(limit = 20, time = 1, timeUnit = TimeUnit.HOURS, errorMessage = "普通翻译文本请求达到阈值（1小时/20次！")
    Result<TranslationVO> tencentTranslation(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @Valid @RequestBody TranslationDTO dto) {
        return Result.ok(resService.translationText(dto));
    }

    @Operation(summary = "流式翻译文本(sse)")
    @PostMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ReqPermission(name = "流式翻译文本(sse)", intro = "用户消费者翻译文本(sse)", expression = "res:utils:translation:sse")
    @PortFlowControl(limit = 10, time = 10, timeUnit = TimeUnit.MINUTES, errorMessage = "流式翻译文本过于频繁（10分钟/10次！")
    SseEmitterUTF8 tencentTranslationSSE(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @Valid @RequestBody TranslationDTO dto) {
        return resService.translationTextSSE(dto);
    }
}
