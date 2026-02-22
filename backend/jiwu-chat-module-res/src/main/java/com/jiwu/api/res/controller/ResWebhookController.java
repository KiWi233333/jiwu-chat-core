package com.jiwu.api.res.controller;

import com.jiwu.api.common.util.service.SmsUtil;
import com.jiwu.api.common.webhook.domain.dto.SmsDeliveryReportDTO;
import com.jiwu.api.common.webhook.domain.dto.UniSmsDlrDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "资源模块/工具模块/webhook")
@RestController
@RequestMapping("/res/utils/webhook")
public class ResWebhookController {

    // 短信web
    private final SmsUtil smsUtil;

    @Autowired
    public ResWebhookController(SmsUtil smsUtil) {
        this.smsUtil = smsUtil;
    }

    @Operation(summary = "短信状态回调", tags = {"webhook"})
    @PostMapping("/sms")
    public ResponseEntity<String> handleDlr(
            @RequestBody UniSmsDlrDTO dto,
            @RequestHeader(value = "Authorization") String authorization) {

        log.info("收到短信状态报告(DLR): {}", dto);
        try {
            // 验证请求签名（如果启用了签名验证）
            if (smsUtil.verifyWebhookSignature(dto, authorization)) {
                // 转换为通用DTO
                SmsDeliveryReportDTO reportDTO = dto.toSmsDeliveryReportDTO();

                // 处理DLR逻辑
                processDlr(reportDTO);
                return ResponseEntity.ok("DLR received successfully");
            } else {
                log.warn("DLR签名验证失败");
                return ResponseEntity.badRequest().body("Invalid signature");
            }
        } catch (Exception e) {
            log.error("处理DLR失败", e);
            return ResponseEntity.internalServerError().body("Failed to process DLR");
        }
    }

    /**
     * 处理短信状态报告的具体业务逻辑
     * @param reportDTO DLR数据传输对象
     */
    private void processDlr(SmsDeliveryReportDTO reportDTO) {
        // 根据状态进行处理
        switch (reportDTO.getStatus()) {
            case "delivered":
                log.info("短信已送达: messageId={}, mobile={}", reportDTO.getMessageId(), reportDTO.getMobile());
                break;
            case "undelivered":
                log.warn("短信未送达: messageId={}, mobile={}", reportDTO.getMessageId(), reportDTO.getMobile());
                break;
            case "rejected":
                log.warn("短信被拒绝: messageId={}, mobile={}, errorCode={}", 
                        reportDTO.getMessageId(), reportDTO.getMobile(), reportDTO.getErrorCode());
                break;
            default:
                log.info("其他状态: status={}, messageId={}, mobile={}", 
                        reportDTO.getStatus(), reportDTO.getMessageId(), reportDTO.getMobile());
                break;
        }
        
        // 在这里可以添加更多业务逻辑，如更新数据库记录等
    }
}
