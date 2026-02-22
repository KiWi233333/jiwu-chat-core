package com.jiwu.api.common.constant;

/**
 * 用户Redis keys
 *
 * @className: UserConstant
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/4/27 18:56
 */
public class UserConstant {
    /**
     * 用户token key
     **/
    public static final String USER_ID_KEY = "userId";
    public static final String USER_AGENT_KEY = "User-Agent";

    /**
     * 盐表
     **/
    private static final String USER_SALT_DTO_KEY = "user:salts:";// 用户全部盐表 +userId:userType
    public static String userSaltDTOKey(String userId) {
        return USER_SALT_DTO_KEY + userId;
    }

    /**
     * 验证码
     **/
    public static final String PHONE_CODE_KEY = "user:login:code:";// 登录|校验 临时手机号验证码
    public static final String EMAIL_CODE_KEY = "user:login:code:";// 登录|校验 临时邮箱验证码
    public static final String PHONE_CHECK_CODE_KEY = "user:register:code:";// 注册临时手机号验证码
    public static final String EMAIL_CHECK_CODE_KEY = "user:register:code:";// 注册临时邮箱验证码

    /**
     * 用户信息
     **/
    public static final String USER_ROLE_KEY = "user:role:";// map 角色权限  + userId
    public static final String USERNAME_MAPS_KEY = "user:usernames:";// 用户名
    public static final String PHONE_MAPS_KEY = "user:phones:";// 手机号
    public static final String EMAIL_MAPS_KEY = "user:emails:";// 邮箱

    /**
     * 用户
     **/
    public static final String USER_KEY = "user:info:";// 用户详细信息
    public static final String USER_LIST_KEY = "user:list:";// 用户列表

    /**
     * 登录token
     **/
    public static final String USER_REFRESH_TOKEN_KEY = "user:refresh:token:";// 用户refresh_token key + id
    public static final String USER_ACCESS_TOKEN_KEY = "user:access:token:";// 用户Token

    /**
     * 用户钱包
     **/
    public static final String USER_WALLET_KEY = "user:wallet:";// 钱包信息
    public static final String USER_RECHARGE_COMBO_KEY = "user:wallet:combo";// 充值套餐信息
    /**
     * 用户地址
     **/
    public static final String USER_ADDRESS_PAGE_KEY = "user:address:";// 地址信息

}
