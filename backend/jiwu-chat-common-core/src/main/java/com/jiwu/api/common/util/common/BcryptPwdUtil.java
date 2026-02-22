package com.jiwu.api.common.util.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密解密工具类
 *
 * @className: BcryptPwdUtil
 * @author: Author作者
 * @date: 2023/4/19 17:00
 */
public class BcryptPwdUtil {

    private static final int SALT_LEN = 10;

    /**
     * 生成随机盐和加密的密码
     *
     * @param password 原密码
     * @return 加密密码
     */
    public static String encode(String password) {
        String salt = getRandomSalt();// 生成随机盐
        return encodeBySalt(password, salt);// 加密密码
    }

    /**
     * 提供盐加密密码
     *
     * @param password 原密码
     * @param salt 盐值
     * @return str
     */
    public static String encodeBySalt(String password, String salt) {
        String saltedPassword = salt + password;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(saltedPassword);// 加密
    }

    /**
     * 验证密码是否正确
     *
     * @param password 原密码
     * @param encodedPassword 加密密码
     * @param salt 盐值
     * @return boolean
     */
    public static boolean matches(String password, String encodedPassword, String salt) {
        String saltedPassword = salt + password;// 组成盐
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        try {
            return encoder.matches(saltedPassword, encodedPassword);
        }catch (Exception e ) {
            return false;
        }
    }

    /**
     * 生成随机盐
     * @return str
     */
    public static String getRandomSalt() {
        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
