package com.jiwu.api.common.main.cache.user;

import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import org.springframework.stereotype.Component;

/**
 * Description: 用户盐值密码信息缓存（简单KV存储）
 * Date: 2025-11-19
 */
@Component
public class UserSaltCache {
    
    /**
     * 删除用户盐值缓存
     */
    public void delUserSalt(String userId) {
        String key = UserConstant.userSaltDTOKey(userId);
        RedisStaticUtil.del(key);
    }
    
    /**
     * 批量删除用户盐值缓存
     */
    public void batchDelUserSalt(String... userIds) {
        for (String userId : userIds) {
            delUserSalt(userId);
        }
    }
}
