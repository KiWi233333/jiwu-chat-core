package com.jiwu.api.common.main.cache.user;

import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Description: 用户邮箱缓存管理
 * Date: 2025-11-19
 */
@Component
public class UserEmailCache {
    
    /**
     * 邮箱缓存类型枚举
     */
    @Getter
    @AllArgsConstructor
    public enum EmailCacheType {
        /** 邮箱映射 */
        MAPPING(UserConstant.EMAIL_MAPS_KEY, 7 * 24 * 60 * 60L),
        /** 登录验证码 */
        LOGIN_CODE(UserConstant.EMAIL_CODE_KEY, 5 * 60L),
        /** 注册/修改验证码 */
        CHECK_CODE(UserConstant.EMAIL_CHECK_CODE_KEY, 5 * 60L);
        
        private final String keyPrefix;
        private final Long defaultTtl; // 默认过期时间（秒）
    }
    
    /**
     * 通用设置缓存方法
     */
    private void setCache(EmailCacheType type, String email, String value, Long ttl) {
        String key = type.getKeyPrefix() + email;
        long expireSeconds = ttl != null ? ttl : type.getDefaultTtl();
        RedisStaticUtil.set(key, value, expireSeconds);
    }
    
    /**
     * 通用获取缓存方法
     */
    private String getCache(EmailCacheType type, String email) {
        String key = type.getKeyPrefix() + email;
        return RedisStaticUtil.get(key, String.class);
    }
    
    /**
     * 通用删除缓存方法
     */
    private void delCache(EmailCacheType type, String email) {
        String key = type.getKeyPrefix() + email;
        RedisStaticUtil.del(key);
    }
    
    // ========== 邮箱映射相关 ==========
    
    /**
     * 设置邮箱映射缓存
     */
    public void setEmailMapping(String email, String userId) {
        setCache(EmailCacheType.MAPPING, email, userId, null);
    }
    
    /**
     * 获取邮箱对应的用户ID
     */
    public String getEmailMapping(String email) {
        return getCache(EmailCacheType.MAPPING, email);
    }
    
    /**
     * 删除邮箱映射缓存
     */
    public void delEmailMapping(String email) {
        delCache(EmailCacheType.MAPPING, email);
    }
    
    // ========== 登录验证码相关 ==========
    
    /**
     * 设置邮箱验证码（登录用）
     */
    public void setLoginCode(String email, String code, long ttl, TimeUnit timeUnit) {
        setCache(EmailCacheType.LOGIN_CODE, email, code, timeUnit.toSeconds(ttl));
    }
    
    /**
     * 获取邮箱验证码（登录用）
     */
    public String getLoginCode(String email) {
        return getCache(EmailCacheType.LOGIN_CODE, email);
    }
    
    /**
     * 删除邮箱验证码（登录用）
     */
    public void delLoginCode(String email) {
        delCache(EmailCacheType.LOGIN_CODE, email);
    }
    
    // ========== 注册/修改验证码相关 ==========
    
    /**
     * 设置邮箱验证码（注册/修改用）
     */
    public void setCheckCode(String email, String code, long ttl, TimeUnit timeUnit) {
        setCache(EmailCacheType.CHECK_CODE, email, code, timeUnit.toSeconds(ttl));
    }
    
    /**
     * 获取邮箱验证码（注册/修改用）
     */
    public String getCheckCode(String email) {
        return getCache(EmailCacheType.CHECK_CODE, email);
    }
    
    /**
     * 删除邮箱验证码（注册/修改用）
     */
    public void delCheckCode(String email) {
        delCache(EmailCacheType.CHECK_CODE, email);
    }
}
