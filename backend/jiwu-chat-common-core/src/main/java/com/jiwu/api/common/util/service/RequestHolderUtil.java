package com.jiwu.api.common.util.service;


import com.jiwu.api.common.util.service.auth.UserTokenDTO;

/**
 * Description: 请求上下文
 * Date: 2023-04-05
 */
public class RequestHolderUtil {

    private static final ThreadLocal<UserTokenDTO> threadLocal = new ThreadLocal<>();

    public static void set(UserTokenDTO requestInfo) {
        threadLocal.set(requestInfo);
    }

    public static UserTokenDTO get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }


}
