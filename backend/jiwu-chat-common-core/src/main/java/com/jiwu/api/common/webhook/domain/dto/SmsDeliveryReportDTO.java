package com.jiwu.api.common.webhook.domain.dto;

import lombok.Data;
import java.util.Date;

/**
 * 短信状态报告(DLR)数据传输对象
 */
@Data
public class SmsDeliveryReportDTO {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 手机号码
     */
    private String mobile;
    
    /**
     * 状态
     * - delivered: 已送达
     * - undelivered: 未送达
     * - rejected: 被拒绝
     */
    private String status;
    
    /**
     * 错误码（如果有）
     */
    private String errorCode;
    
    /**
     * 状态更新时间
     */
    private Date statusTime;
    
    /**
     * 请求签名
     */
    private String signature;
    
    /**
     * 短信提供商（例如：UniSMS）
     */
    private String provider;
} 
