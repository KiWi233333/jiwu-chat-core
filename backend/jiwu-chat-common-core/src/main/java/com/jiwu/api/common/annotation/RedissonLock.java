package com.jiwu.api.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Redisson分布式锁注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {
    /**
     * 锁的key，支持SpEL表达式
     */
    String key();

    /**
     * 等待时间，默认-1，表示一直等待
     */
    long waitTime() default -1;

    /**
     * 锁超时时间，默认-1，看门狗自动续期
     */
    long leaseTime() default -1;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 锁的前缀
     */
    String prefix() default "lock:";
}
