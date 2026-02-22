package com.jiwu.api.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 频控注解
 *
 * @https://blog.csdn.net/liangsheng_g/article/details/113180629
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowControl {
    /**
     * redis key前缀值
     */
    String value() default "other:flow:";

    /**
     * redis key 后缀值
     */
    String spEl() default "";

    /**
     * key的方式
     */
    Target target() default Target.AUTO;

    /**
     * 错误code
     */
    int errorCode() default 40004;

    /**
     * 错误消息
     */
    String errorMsg() default "系统繁忙，请稍后重试！";

    /**
     * 时间内可执行次数
     */
    long limit() default 1;

    /**
     * 节流时间（默认秒）
     */
    long duration() default 60;

    /**
     * 节流时间单位（默认秒）
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;  //时间单位

    enum Target {
        AUTO,
        EL;
    }
}
