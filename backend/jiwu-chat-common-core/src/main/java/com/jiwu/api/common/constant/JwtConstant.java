package com.jiwu.api.common.constant;


public class JwtConstant {//该类的常量值要根据具体的项目进行设置
    /* 请求头相关 */
    public static final String HEADER_NAME = "Authorization"; //前端页面将token存放在请求头Header的Authorization中
    public static final String ALLOW_SECRET_KEY_NAME = "Secret-Key"; // Secret密钥
    public static final String ISSUER = "kiwi2333";
    public static final String SUBJECT_OBJ = "kiwi2333";
    public static final String SECRET_KEY = "kihida23@wae3x"; // Secret密钥
    public static final String SAVE_OBJ_KEY = "user";
    public static final Integer TOKEN_TIME = 60 * 24 * 14; //   token过期时间(天)
    public static final Integer REDIS_TOKEN_TIME = 60 * 24; //  24小时 redis token过期时间(分钟)
    public static final Long TOKEN_REFRESH_DAYS = 7L;

}

