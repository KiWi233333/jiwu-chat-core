package com.jiwu.api.common.main.cache.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import com.jiwu.api.common.main.dao.sys.UserRoleDAO;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.RedisKeyUtil;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 用户相关缓存
 * Date: 2023-03-27
 */
@Component
public class UserCache {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleDAO userRoleDAO;
    //     用户的信息更新时间
    public static final String CHAT_USER_INFO_MAP = "chat:user:info:map:%s";

    // 用户的信息更新时间
    public static final String CHAT_USER_MODIFY_MAP = "chat:user:modify:uid:%s";
    // 在线用户
    public static final String CHAT_ONLINE_UID_ZET = "chat:user:online:zet";
    public static final String CHAT_OFFLINE_UID_ZET = "chat:user:offline:zet";

    public Long getOnlineNum() {
        String onlineKey = RedisKeyUtil.getKey(CHAT_ONLINE_UID_ZET);
        return RedisStaticUtil.zCard(onlineKey);
    }

    public Long getOfflineNum() {
        String offlineKey = RedisKeyUtil.getKey(CHAT_OFFLINE_UID_ZET);
        return RedisStaticUtil.zCard(offlineKey);
    }

    //移除用户
    public void remove(String uid) {
        String onlineKey = RedisKeyUtil.getKey(CHAT_ONLINE_UID_ZET);
        String offlineKey = RedisKeyUtil.getKey(CHAT_OFFLINE_UID_ZET);
        //移除离线表
        RedisStaticUtil.zRemove(offlineKey, uid);
        //移除上线表
        RedisStaticUtil.zRemove(onlineKey, uid);
    }

    //用户上线
    public void online(String uid, Date optTime) {
        //移除离线表
        RedisStaticUtil.zRemove(CHAT_OFFLINE_UID_ZET, uid);
        //更新上线表
        RedisStaticUtil.zAdd(CHAT_ONLINE_UID_ZET, uid, optTime.getTime());
    }

    //获取用户上线列表
    public List<String> getOnlineUidList() {
        Set<String> strings = RedisStaticUtil.zAll(CHAT_ONLINE_UID_ZET);
        return strings.stream().map(String::valueOf).collect(Collectors.toList());
    }

    public boolean isOnline(String uid) {
        return RedisStaticUtil.zIsMember(CHAT_ONLINE_UID_ZET, uid);
    }

    //用户下线
    public void offline(String uid, Date optTime) {
        //移除上线线表
        RedisStaticUtil.zRemove(CHAT_ONLINE_UID_ZET, uid);
        //更新上线表
        RedisStaticUtil.zAdd(CHAT_OFFLINE_UID_ZET, uid, optTime.getTime());
    }

    public CursorPageBaseVO<Pair<String, Double>> getOnlineCursorPage(CursorPageBaseDTO pageBaseReq) {
        return CursorUtils.getCursorPageByRedis(pageBaseReq, CHAT_ONLINE_UID_ZET, String::valueOf);
    }

    public CursorPageBaseVO<Pair<String, Double>> getOfflineCursorPage(CursorPageBaseDTO pageBaseReq) {
        return CursorUtils.getCursorPageByRedis(pageBaseReq, RedisKeyUtil.getKey(CHAT_OFFLINE_UID_ZET), String::valueOf);
    }

    public List<String> getUserModifyTime(List<String> uidList) {
        List<String> keys = uidList.stream().map(uid -> RedisKeyUtil.getKey(CHAT_USER_MODIFY_MAP, uid)).collect(Collectors.toList());
        return RedisStaticUtil.mget(keys, String.class);
    }

    public void refreshUserModifyTime(String uid) {
        String key = RedisKeyUtil.getKey(CHAT_USER_MODIFY_MAP, uid);
        RedisStaticUtil.set(key, new Date().getTime());
    }

    /**
     * 获取用户信息，盘路缓存模式
     */
    public User getUserInfo(String uid) {//todo 后期做二级缓存
        return getUserInfoBatch(Collections.singleton(uid)).get(uid);
    }

    // 计算用户数
    public int getUidExistsCount(List<String> uidList) {
        final Map<String, User> userInfoBatch = getUserInfoBatch(new HashSet<>(uidList));
        return userInfoBatch.keySet().size();
    }

    /**
     * 获取用户信息，盘路缓存模式
     */
    public Map<String, User> getUserInfoBatch(Set<String> uids) {
        //批量组装key
        List<String> keys = uids.stream().map(id -> RedisKeyUtil.getKey(CHAT_USER_INFO_MAP, id)).collect(Collectors.toList());
        //批量get
        List<User> mget = RedisStaticUtil.mget(keys, User.class);
        Map<String, User> map = mget.stream().filter(Objects::nonNull).collect(Collectors.toMap(User::getId, Function.identity()));
        //发现差集——还需要load更新的uid
        List<String> needLoadUidList = uids.stream().filter(a -> !map.containsKey(a)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(needLoadUidList)) {
            //批量load
            List<User> needLoadUserList = userMapper.selectBatchIds(needLoadUidList);
            Map<String, User> redisMap = needLoadUserList.stream().collect(Collectors.toMap(a -> RedisKeyUtil.getKey(CHAT_USER_INFO_MAP, a.getId()), Function.identity()));
            RedisStaticUtil.mset(redisMap, 1200); //  设置缓存时间为15分钟
            //加载回redis
            map.putAll(needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity())));
        }
        return map;
    }


    /**
     * 获取用户信息，盘路缓存模式
     */
    public Map<String, User> getUserInfoBatch(List<String> uids) {
        //批量组装key
        List<String> keys = uids.stream().map(id -> RedisKeyUtil.getKey(CHAT_USER_INFO_MAP, id)).collect(Collectors.toList());
        //批量get
        List<User> mget = RedisStaticUtil.mget(keys, User.class);
        Map<String, User> map = mget.stream().filter(Objects::nonNull).collect(Collectors.toMap(User::getId, Function.identity()));
        //发现差集——还需要load更新的uid
        List<String> needLoadUidList = uids.stream().filter(a -> !map.containsKey(a)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(needLoadUidList)) {
            //批量load
            List<User> needLoadUserList = userMapper.selectBatchIds(needLoadUidList);
            Map<String, User> redisMap = needLoadUserList.stream().collect(Collectors.toMap(a -> RedisKeyUtil.getKey(CHAT_USER_INFO_MAP, a.getId()), Function.identity()));
            RedisStaticUtil.mset(redisMap, 60 * 1000 * 8); //  设置缓存时间为8分钟
            //加载回redis
            map.putAll(needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity())));
        }
        return map;
    }

    public void delUserInfo(String uid) {
        String key = RedisKeyUtil.getKey(CHAT_USER_INFO_MAP, uid);
        RedisStaticUtil.del(key);
    }


    public static final String USER_INFO_KEY = "sys:user:role:";

    /******************************* 权限缓存 ****************************/
    @Cacheable(cacheNames = USER_INFO_KEY, key = "'code:'+#uid")
    public Set<String> getRoleCodeSet(String uid) {

        List<Role> userRoles = userRoleDAO.selectUserRoleList(uid);
        return userRoles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }
}
