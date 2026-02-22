package com.jiwu.api.common.config;

import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.unisms.config.UniConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * SMS4J配置类
 */
@Configuration
public class Sms4jConfiguration {

    private static final Logger log = LoggerFactory.getLogger(Sms4jConfiguration.class);

    @Value("${sms.uni-sms.access-key}")
    private String accessKeyId;

    @Value("${sms.uni-sms.secret-key}")
    private String accessKeySecret;

    @Value("${sms.uni-sms.signature}")
    private String signature;

    @Value("${sms.uni-sms.default-templated-id}")
    private String templateId;

    @Value("${sms.sms4j.configId:defaultConfig}")
    private String configId;

    @Value("${sms.provider:unisms}")
    private String smsProvider;

    /**
     * 初始化SMS4J配置
     */
    @PostConstruct
    public void init() {
        // 只有当选择SMS4J作为服务提供商时才进行初始化
        if ("sms4j".equalsIgnoreCase(smsProvider)) {
            try {
                // 创建UniSMS配置
                UniConfig config = new UniConfig();
                // 设置AccessKey
                config.setAccessKeyId(accessKeyId);
                // 设置SecretKey
                config.setAccessKeySecret(accessKeySecret);
                // 设置短信签名
                config.setSignature(signature);
                // 设置默认模板ID
                config.setTemplateId(templateId);
                // 设置配置ID
                config.setConfigId(configId);
                
                // 创建SMS4J实例
                SmsFactory.createSmsBlend(config);
                
                // 不再使用addCallback，因为新版本不支持该方法
                log.info("SMS4J初始化完成，配置ID: {}", configId);
            } catch (Exception e) {
                log.error("SMS4J初始化失败: {}", e.getMessage(), e);
            }
        } else {
            log.info("当前服务提供商为: {}，跳过SMS4J初始化", smsProvider);
        }
    }
} 
