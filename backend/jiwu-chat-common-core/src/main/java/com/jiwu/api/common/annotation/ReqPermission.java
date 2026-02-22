package com.jiwu.api.common.annotation;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqPermission {


    /**
     * 权限名称
     */
    String name();
    /**
     * 表达式
     */
    String expression();
    /**
     * 备注
     */
    String intro() default "";
}
