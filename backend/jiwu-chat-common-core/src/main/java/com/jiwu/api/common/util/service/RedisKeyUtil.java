package com.jiwu.api.common.util.service;

/**
 * 描述
 *
 * @className: RedisKeyUtils
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/18 9:52
 */
public class RedisKeyUtil {
    public static String getKey(String key, Object... objects) {
        return String.format(key, objects);
    }
}
