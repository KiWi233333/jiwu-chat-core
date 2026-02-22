package com.jiwu.api.common.util.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 描述
 *
 * @className: BcrypUtil
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/7/29 17:07
 */
public class BcryptUtil {

    static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 使用BCrypt加密信息
     * @param str 加密对象
     * @return str
     */
    public static String getBcryptStr(String str) {
        return passwordEncoder.encode(str);
    }
}
