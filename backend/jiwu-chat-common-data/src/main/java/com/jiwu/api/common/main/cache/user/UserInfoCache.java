package com.jiwu.api.common.main.cache.user;

import com.jiwu.api.common.cache.AbstractRedisStringCache;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.RedisKeyUtil;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 用户基本信息的缓存
 * Date: 2023-06-10
 */
@Component
public class UserInfoCache extends AbstractRedisStringCache<String, User> {
    @Resource
    private UserMapper userMapper;
    @Override
    protected String getKey(String uid) {
        return RedisKeyUtil.getKey(UserCache.CHAT_USER_INFO_MAP, uid);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<String, User> load(List<String> uidList) {
        List<User> needLoadUserList = userMapper.selectBatchIds(uidList);
        return needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
