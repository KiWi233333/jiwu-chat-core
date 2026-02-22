package com.jiwu.api.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略身份验证注解
 * 
 * @author Kiwi23333
 * @description 用于标记不需要身份验证的接口
 * @date 2025/5/30
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAuth {
    /**
     * 描述信息
     */
    String value() default "";
}
