package com.jiwu.api.common.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Inherited
@Documented
@Target({ElementType.FIELD,ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PortFlowControl {

    /**
     * 标识 指定sec时间段内的访问次数限制
     */
    int limit() default 6;
    /**
     * 节流时间（默认秒）
     */
    long time() default 6;
    /**
     * 节流时间单位（默认秒）
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;  //时间单位

    /**
     * redis存储方式
     */
    KeyType keyType() default KeyType.IP;
    /**
     * 错误提醒文字
     */

    String errorMessage() default "请求太频繁，请稍后重试！";  // 错误消息

    enum KeyType {
        IP
    }
}
