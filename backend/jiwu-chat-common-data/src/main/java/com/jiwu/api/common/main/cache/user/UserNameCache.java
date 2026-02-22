package com.jiwu.api.common.main.cache.user;

import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Description: 用户名缓存管理
 * Date: 2025-11-19
 */
@Component
public class UserNameCache {
    
    /**
     * 用户名缓存类型枚举
     */
    @Getter
    @AllArgsConstructor
    public enum UsernameCacheType {
        /** 用户名映射 */
        MAPPING(UserConstant.USERNAME_MAPS_KEY, 7 * 24 * 60 * 60L);
        
        private final String keyPrefix;
        private final Long defaultTtl; // 默认过期时间（秒）
    }
    
    /**
     * 通用设置缓存方法
     */
    private void setCache(UsernameCacheType type, String username, String value, Long ttl) {
        String key = type.getKeyPrefix() + username;
        long expireSeconds = ttl != null ? ttl : type.getDefaultTtl();
        RedisStaticUtil.set(key, value, expireSeconds);
    }
    
    /**
     * 通用获取缓存方法
     */
    private String getCache(UsernameCacheType type, String username) {
        String key = type.getKeyPrefix() + username;
        return RedisStaticUtil.get(key, String.class);
    }
    
    /**
     * 通用删除缓存方法
     */
    private void delCache(UsernameCacheType type, String username) {
        String key = type.getKeyPrefix() + username;
        RedisStaticUtil.del(key);
    }
    
    // ========== 用户名映射相关 ==========
    
    /**
     * 设置用户名映射缓存
     */
    public void setUsernameMapping(String username, String userId) {
        setCache(UsernameCacheType.MAPPING, username, userId, null);
    }
    
    /**
     * 获取用户名对应的用户ID
     */
    public String getUsernameMapping(String username) {
        return getCache(UsernameCacheType.MAPPING, username);
    }
    
    /**
     * 删除用户名映射缓存
     */
    public void delUsernameMapping(String username) {
        delCache(UsernameCacheType.MAPPING, username);
    }
}
