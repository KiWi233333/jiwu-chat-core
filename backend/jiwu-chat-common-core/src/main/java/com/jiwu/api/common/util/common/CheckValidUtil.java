package com.jiwu.api.common.util.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述
 *
 * @className: CheckVaildUtil
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/4/28 0:03
 */
public class CheckValidUtil {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_PATTERN = "^(13\\d|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18\\d|19[0-35-9])\\d{8}$";
    private static final String USERNAME_PATTERN = "^[a-zA-Z_]\\w*$";;
    // 邮箱验证
    public static boolean checkEmail(final String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // 手机号验证
    public static boolean checkPhone(String phone) {
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    // 用户名验证
    public static boolean checkUsername(String username) {
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
}
