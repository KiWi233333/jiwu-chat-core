package com.jiwu.api.common.annotation;

import com.jiwu.api.common.annotation.validator.NullableLengthValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可空长度校验注解
 * <p>
 * 当字段值为 null 或空字符串时跳过校验，有值时才校验长度范围和正则表达式
 *
 * @author Kiwi23333
 * @date 2025/12/06
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NullableLengthValidator.class)
public @interface NullableLength {

    /**
     * 最小长度
     */
    int min() default 0;

    /**
     * 最大长度
     */
    int max() default Integer.MAX_VALUE;

    /**
     * 正则表达式（可选，为空则不校验正则）
     */
    String regexp() default "";

    /**
     * 校验失败消息
     */
    String message() default "格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
