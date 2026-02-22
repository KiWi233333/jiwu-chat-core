package com.jiwu.api.common.util.service;

import com.apistd.uni.Uni;
import com.apistd.uni.UniException;
import com.apistd.uni.UniResponse;
import com.apistd.uni.sms.UniMessage;
import com.apistd.uni.sms.UniSMS;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.webhook.domain.dto.UniSmsDlrDTO;

import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;

/**
 * 短信服务工具类
 * 支持UniSMS和SMS4J两种实现
 *
 * @className: SmsUtil
 * @author: Kiwi23333
 * @description: 短信服务工具类，支持UniSMS和SMS4J
 * @date: 2023/7/27 21:07
 */
@Slf4j
@Component
public class SmsUtil {

    @Value("${sms.uni-sms.access-key}")
    private String ACCESS_KEY_ID;
    @Value("${sms.uni-sms.secret-key}")
    private String ACCESS_KEY_SECRET;
    @Value("${sms.uni-sms.default-templated-id}")
    private String DEFAULT_TEMPLATED_ID;
    //    @Value("${sms.uni-sms.signature}")
    private String SIGNATURE = " 我的个人站";

    @Value("${sms.uni-sms.webhook.signature-secret:#{null}}")
    private String WEBHOOK_SIGNATURE_SECRET;

    @Value("${sms.uni-sms.webhook.enabled:false}")
    private boolean webhookEnabled;

    // 选择使用哪种SMS实现，默认为UniSMS
    @Value("${sms.provider:unisms}")
    private String smsProvider;

    // SMS4J配置ID，用于获取对应的SMS4J配置
    @Value("${sms.sms4j.configId:defaultConfig}")
    private String sms4jConfigId;


    // 是否使用SMS4J
    private boolean useSms4j = false;

    /**
     * 初始化方法，根据配置决定使用哪种SMS实现
     */
    @PostConstruct
    public void init() {
        // 确定使用哪种SMS实现
        useSms4j = "sms4j".equalsIgnoreCase(smsProvider);
        
        log.info("SMS服务初始化，使用的服务提供商: {}", useSms4j ? "SMS4J" : "UniSMS");
        
        // 如果使用SMS4J，检查其配置是否存在
        if (useSms4j) {
            try {
                SmsBlend smsBlend = SmsFactory.getSmsBlend(sms4jConfigId);
                if (smsBlend != null) {
                    log.info("SMS4J配置加载成功，配置ID: {}", sms4jConfigId);
                } else {
                    log.warn("SMS4J配置加载失败，将默认使用UniSMS");
                    useSms4j = false;
                }
            } catch (Exception e) {
                log.error("SMS4J初始化错误，将默认使用UniSMS: {}", e.getMessage());
                useSms4j = false;
            }
        }
    }

    public enum MsgType {
        /**
         * 登录
         */
        LOGIN,
        /**
         * 注册绑定手机号
         */
        REGISTER,
        /**
         * 重置密码
         */
        RESET_PWD,
        /**
         * 修改密码
         */
        UPDATE_PWD,
        /**
         * 绑定手机号
         */
        BIND_PHONE,
        /**
         * 更换绑定手机号
         */
        RESET_BIND_PHONE;
    }

    /**
     * 自动识别发送短信
     * @param phone 手机号
     * @param code 验证码
     * @param ttl 有效期（分钟）
     * @param type 类型模板
     * @return 是否完成发送
     */
    public boolean autoSendByType(String phone, String code, long ttl, MsgType type) {
        String templateId = "pub_verif_login_ttl";
        switch (type) {
            case LOGIN:
                templateId = "pub_verif_login_ttl";
                break;
            case REGISTER:
                templateId = "pub_verif_register_ttl";
                break;
            case RESET_PWD:
                templateId = "pub_verif_forgetpass_ttl";
                break;
            case UPDATE_PWD:
                templateId = "pub_verif_recoverpass_ttl";
                break;
            case BIND_PHONE:
                templateId = "pub_verif_bindmob_ttl";
                break;
            case RESET_BIND_PHONE:
                templateId = "pub_verif_rebindmob_ttl";
                break;
        }
        return send(phone, code, ttl, "", templateId);
    }

    /**
     * 发送短信
     *
     * @param phone      手机号
     * @param code       验证码
     * @param ttl        分钟数
     * @param msg        消息
     * @param templateId 模板id
     * @return boolean 是否完成
     */
    public boolean send(String phone, String code, long ttl, String msg, String templateId) {
        // 根据配置决定使用哪种SMS实现
        if (useSms4j) {
            return sendBySms4j(phone, code, ttl, msg, templateId);
        } else {
            return sendByUniSms(phone, code, ttl, msg, templateId);
        }
    }

    /**
     * 使用UniSMS发送短信
     *
     * @param phone      手机号
     * @param code       验证码
     * @param ttl        分钟数
     * @param msg        消息
     * @param templateId 模板id
     * @return boolean 是否完成
     */
    private boolean sendByUniSms(String phone, String code, long ttl, String msg, String templateId) {
        // 初始化
        Uni.init(ACCESS_KEY_ID, ACCESS_KEY_SECRET); // 若使用简易验签模式仅传入第一个参数即可
        // 设置自定义参数 (变量短信)
        Map<String, String> templateData = new HashMap<>();
        templateData.put("code", code);
        templateData.put("ttl", String.valueOf(ttl));
        // 构建信息
        UniMessage message = UniSMS.buildMessage()
                .setTo(phone)
                .setSignature(SIGNATURE)
                .setTemplateId(org.springframework.util.StringUtils.hasText(templateId) ? templateId : DEFAULT_TEMPLATED_ID)
                .setTemplateData(templateData);
        // 发送短信
        try {
            UniResponse res = message.send();
            log.info("UniSMS发送短信成功：{}", res.message);
            return res.code.equals("0");
        } catch (UniException e) {
            log.warn("UniSMS发送短信失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 使用SMS4J发送短信
     *
     * @param phone      手机号
     * @param code       验证码
     * @param ttl        分钟数
     * @param msg        消息
     * @param templateId 模板id
     * @return boolean 是否完成
     */
    private boolean sendBySms4j(String phone, String code, long ttl, String msg, String templateId) {
        try {
            // 获取SMS4J实例
            SmsBlend smsBlend = SmsFactory.getSmsBlend(sms4jConfigId);
            if (smsBlend == null) {
                log.error("SMS4J配置未找到，配置ID: {}", sms4jConfigId);
                return false;
            }

            // 构建参数
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("code", code);
            params.put("ttl", String.valueOf(ttl));
            if (org.springframework.util.StringUtils.hasText(msg)) {
                params.put("msg", msg);
            }

            // 发送短信
            SmsResponse response;
            if (org.springframework.util.StringUtils.hasText(templateId)) {
                // 使用指定模板ID发送
                response = smsBlend.sendMessage(phone, templateId, params);
            } else {
                // 使用默认模板ID发送
                response = smsBlend.sendMessage(phone, params);
            }

            // 处理响应
            if (response.isSuccess()) {
                log.info("SMS4J发送短信成功：手机号={}, 响应={}", phone, response);
                return true;
            } else {
                log.warn("SMS4J发送短信失败：手机号={}, 响应={}", phone, response);
                return false;
            }
        } catch (Exception e) {
            log.error("SMS4J发送短信异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证UniSMS Webhook签名
     * 按照UniSMS官方文档要求的验证流程实现:
     * 1. 从Authorization头中提取Timestamp, Nonce, Signature
     * 2. 将Timestamp, Nonce字段添加到请求有效载荷
     * 3. 按字典序排序参数，构建待签名字符串
     * 4. 使用HmacSHA256算法和签名密钥计算签名
     * 5. 比对计算出的签名与请求中的签名
     * 
     * @param dto 短信状态报告DTO
     * @param authHeader Authorization请求头
     * @return 签名是否有效
     */
    public boolean verifyWebhookSignature(UniSmsDlrDTO dto, String authHeader) {
        // 如果webhook未启用，则跳过验证
        if (!webhookEnabled) {
            log.info("UniSMS webhook未启用");
            return true;
        }

        // 如果没有配置签名密钥，则跳过验证
        if (StringUtils.isBlank(WEBHOOK_SIGNATURE_SECRET)) {
            log.warn("未配置UniSMS webhook签名密钥，跳过签名验证");
            return true;
        }

        try {
            // 从Authorization头中提取签名信息
            if (StringUtils.isBlank(authHeader)) {
                log.warn("请求中未包含Authorization头");
                return false;
            }

            // 解析Authorization头
            // 格式: UNI1-HMAC-SHA256 Timestamp=1646634211, Nonce=0702b4ae425b0c2e, Signature=khZU1yxkyedU+va6L1WVgn418ycXs7xz0kxitwjFvl4=
            String timestamp = extractTimestampFromAuthHeader(authHeader);
            String nonce = extractNonceFromAuthHeader(authHeader);
            String signature = extractSignatureFromAuthHeader(authHeader);
            
            if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(nonce) || StringUtils.isBlank(signature)) {
                log.warn("Authorization头中缺少必要参数: timestamp={}, nonce={}, signature={}", timestamp, nonce, signature);
                return false;
            }

            // 构建待签名的字符串
            String stringToSign = buildStringToSignForUniSms(dto, timestamp, nonce);

            // 使用HMAC-SHA256算法计算签名
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    WEBHOOK_SIGNATURE_SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSha256.init(secretKey);
            byte[] hash = hmacSha256.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("验证签名时出错", e);
            return false;
        }
    }
    
    /**
     * 验证UniSMS Webhook签名（不使用头信息，用于兼容）
     * @param dto 短信状态报告DTO
     * @return 签名是否有效
     */
    public boolean verifyWebhookSignature(UniSmsDlrDTO dto) {
        // 为了兼容性，假设没有提供Authorization头，则跳过验证
        log.warn("未提供Authorization头进行验证，将跳过签名验证");
        return true;
    }
    
    /**
     * 从Authorization头中提取签名
     * @param authHeader Authorization头
     * @return 签名值
     */
    private String extractSignatureFromAuthHeader(String authHeader) {
        Pattern pattern = Pattern.compile("Signature=([^,\\s]+)");
        Matcher matcher = pattern.matcher(authHeader);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 从Authorization头中提取时间戳
     * @param authHeader Authorization头
     * @return 时间戳
     */
    private String extractTimestampFromAuthHeader(String authHeader) {
        Pattern pattern = Pattern.compile("Timestamp=([^,\\s]+)");
        Matcher matcher = pattern.matcher(authHeader);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 从Authorization头中提取随机数
     * @param authHeader Authorization头
     * @return 随机数
     */
    private String extractNonceFromAuthHeader(String authHeader) {
        Pattern pattern = Pattern.compile("Nonce=([^,\\s]+)");
        Matcher matcher = pattern.matcher(authHeader);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 按照UniSMS文档要求构建待签名字符串
     * 1. 提取Timestamp, Nonce字段并将字段名转为小写
     * 2. 添加到请求有效载荷中
     * 3. 按字典序(正序)排序参数，构建待签名字符串
     * 
     * @param dto DTO对象
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @return 待签名字符串
     */
    private String buildStringToSignForUniSms(UniSmsDlrDTO dto, String timestamp, String nonce) {
        try {
            // 使用TreeMap确保按字典序排序
            Map<String, String> params = new TreeMap<>();
            
            // 添加DTO中的所有非空字段
            if (dto.getCountryCode() != null) {
                params.put("countryCode", dto.getCountryCode());
            }
            
            if (dto.getCurrency() != null) {
                params.put("currency", dto.getCurrency());
            }
            
            if (dto.getDoneDate() != null) {
                // 将LocalDateTime转为ISO格式并URL编码
                String doneDateStr = dto.getDoneDate().toString().replace(" ", "T") + "Z";
                params.put("doneDate", java.net.URLEncoder.encode(doneDateStr, "UTF-8"));
            }
            
            if (dto.getErrorCode() != null) {
                params.put("errorCode", dto.getErrorCode());
            }
            
            if (dto.getErrorMessage() != null) {
                params.put("errorMessage", java.net.URLEncoder.encode(dto.getErrorMessage(), "UTF-8"));
            }
            
            if (dto.getId() != null) {
                params.put("id", dto.getId());
            }
            
            if (dto.getMessageCount() != null) {
                params.put("messageCount", dto.getMessageCount().toString());
            }
            
            // 添加从Authorization头提取的参数（字段名转为小写）
            params.put("nonce", nonce);
            
            if (dto.getPrice() != null) {
                params.put("price", dto.getPrice().toString());
            }
            
            if (dto.getRegionCode() != null) {
                params.put("regionCode", dto.getRegionCode());
            }
            
            if (dto.getStatus() != null) {
                params.put("status", dto.getStatus());
            }
            
            if (dto.getSubmitDate() != null) {
                // 将LocalDateTime转为ISO格式并URL编码
                String submitDateStr = dto.getSubmitDate().toString().replace(" ", "T") + "Z";
                params.put("submitDate", java.net.URLEncoder.encode(submitDateStr, "UTF-8"));
            }
            
            params.put("timestamp", timestamp);
            
            if (dto.getTo() != null) {
                params.put("to", java.net.URLEncoder.encode(dto.getTo(), "UTF-8"));
            }
            
            // 构建待签名字符串
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            
            String result = sb.toString();
            log.debug("构建的待签名字符串: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("构建待签名字符串出错", e);
            return "";
        }
    }
}
