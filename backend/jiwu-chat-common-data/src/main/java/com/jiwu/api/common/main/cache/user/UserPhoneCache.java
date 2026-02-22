package com.jiwu.api.common.main.cache.user;

import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Description: 用户手机号缓存管理
 * Date: 2025-11-19
 */
@Component
public class UserPhoneCache {
    
    /**
     * 手机号缓存类型枚举
     */
    @Getter
    @AllArgsConstructor
    public enum PhoneCacheType {
        /** 手机号映射 */
        MAPPING(UserConstant.PHONE_MAPS_KEY, 7 * 24 * 60 * 60L),
        /** 登录验证码 */
        LOGIN_CODE(UserConstant.PHONE_CODE_KEY, 30 * 60L),
        /** 注册/修改验证码 */
        CHECK_CODE(UserConstant.PHONE_CHECK_CODE_KEY, 30 * 60L);
        
        private final String keyPrefix;
        private final Long defaultTtl; // 默认过期时间（秒）
    }
    
    /**
     * 通用设置缓存方法
     */
    private void setCache(PhoneCacheType type, String phone, String value, Long ttl) {
        String key = type.getKeyPrefix() + phone;
        long expireSeconds = ttl != null ? ttl : type.getDefaultTtl();
        RedisStaticUtil.set(key, value, expireSeconds);
    }
    
    /**
     * 通用获取缓存方法
     */
    private String getCache(PhoneCacheType type, String phone) {
        String key = type.getKeyPrefix() + phone;
        return RedisStaticUtil.get(key, String.class);
    }
    
    /**
     * 通用删除缓存方法
     */
    private void delCache(PhoneCacheType type, String phone) {
        String key = type.getKeyPrefix() + phone;
        RedisStaticUtil.del(key);
    }
    
    // ========== 手机号映射相关 ==========
    
    /**
     * 设置手机号映射缓存
     */
    public void setPhoneMapping(String phone, String userId) {
        setCache(PhoneCacheType.MAPPING, phone, userId, null);
    }
    
    /**
     * 获取手机号对应的用户ID
     */
    public String getPhoneMapping(String phone) {
        return getCache(PhoneCacheType.MAPPING, phone);
    }
    
    /**
     * 删除手机号映射缓存
     */
    public void delPhoneMapping(String phone) {
        delCache(PhoneCacheType.MAPPING, phone);
    }
    
    // ========== 登录验证码相关 ==========
    
    /**
     * 设置手机验证码（登录用）
     */
    public void setLoginCode(String phone, String code, long ttl, TimeUnit timeUnit) {
        setCache(PhoneCacheType.LOGIN_CODE, phone, code, timeUnit.toSeconds(ttl));
    }
    
    /**
     * 获取手机验证码（登录用）
     */
    public String getLoginCode(String phone) {
        return getCache(PhoneCacheType.LOGIN_CODE, phone);
    }
    
    /**
     * 删除手机验证码（登录用）
     */
    public void delLoginCode(String phone) {
        delCache(PhoneCacheType.LOGIN_CODE, phone);
    }
    
    // ========== 注册/修改验证码相关 ==========
    
    /**
     * 设置手机验证码（注册/修改用）
     */
    public void setCheckCode(String phone, String code, long ttl, TimeUnit timeUnit) {
        setCache(PhoneCacheType.CHECK_CODE, phone, code, timeUnit.toSeconds(ttl));
    }
    
    /**
     * 获取手机验证码（注册/修改用）
     */
    public String getCheckCode(String phone) {
        return getCache(PhoneCacheType.CHECK_CODE, phone);
    }
    
    /**
     * 删除手机验证码（注册/修改用）
     */
    public void delCheckCode(String phone) {
        delCache(PhoneCacheType.CHECK_CODE, phone);
    }
}
