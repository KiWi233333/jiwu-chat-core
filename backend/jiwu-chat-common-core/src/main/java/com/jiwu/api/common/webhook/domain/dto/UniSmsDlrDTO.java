package com.jiwu.api.common.webhook.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UniSMS 短信状态报告(DLR)详细数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniSmsDlrDTO {
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 状态
     * - delivered: 已送达
     * - undelivered: 未送达
     * - rejected: 被拒绝
     */
    private String status;
    
    /**
     * 手机号码（包含国家代码）
     */
    private String to;
    
    /**
     * 地区代码
     */
    private String regionCode;
    
    /**
     * 国家代码
     */
    private String countryCode;
    
    /**
     * 消息计数
     */
    private Integer messageCount;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 货币
     */
    private String currency;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 提交日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime submitDate;
    
    /**
     * 完成日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime doneDate;
    
    /**
     * 请求签名
     */
    private String signature;
    
    /**
     * 将此DTO转换为通用的SmsDeliveryReportDTO
     */
    public SmsDeliveryReportDTO toSmsDeliveryReportDTO() {
        SmsDeliveryReportDTO dto = new SmsDeliveryReportDTO();
        dto.setMessageId(this.id);
        dto.setMobile(this.to);
        dto.setStatus(this.status);
        dto.setErrorCode(this.errorCode);
        dto.setProvider("UniSMS");
        // 如果需要转换日期格式，可以在这里处理
        return dto;
    }
} 
