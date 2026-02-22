package com.jiwu.api.common.constant;

import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.util.service.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 活动相关变量
 *
 * @className: GoodsConstant
 * @author: Kiwi23333
 * @description: 商品相关常量
 * @date: 2023/5/1 17:22
 */
@Component
public class EventConstant {
    /**
     * 活动列表
     **/
    public static final String EVENT_GOODS_MAPS_KEY = "event:goods:maps:";
    public static final String EVENT_LIST_KEY = "event:list:";// 活动列表

    @Autowired
    RedisUtil redisUtil;


    /**
     * 删除活动缓存
     *
     * @param type
     * @return
     */
    public int deleteCatchByListKey(UserType type) {
        int count = 0;
        if (type == null) {
            for (UserType userType : UserType.values()) {
                count += redisUtil.delete(EVENT_LIST_KEY + userType.getCode() + ":") ? 1 : 0;
            }
        } else {
            count += redisUtil.delete(EVENT_LIST_KEY + type + ":") ? 1 : 0;
        }
        return count;
    }
}
