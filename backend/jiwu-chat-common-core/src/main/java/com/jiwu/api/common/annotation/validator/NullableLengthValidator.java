package com.jiwu.api.common.annotation.validator;

import com.jiwu.api.common.annotation.NullableLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * NullableLength 注解校验器
 * <p>
 * 当值为 null 或空字符串时跳过校验，有值时才校验长度范围和正则表达式
 *
 * @author Kiwi23333
 * @date 2025/12/06
 */
public class NullableLengthValidator implements ConstraintValidator<NullableLength, String> {

    private int min;
    private int max;
    private Pattern pattern;

    @Override
    public void initialize(NullableLength constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        String regexp = constraintAnnotation.regexp();
        // 如果正则不为空，则编译正则表达式
        if (regexp != null && !regexp.isEmpty()) {
            this.pattern = Pattern.compile(regexp);
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 值为 null 或空字符串时，跳过校验（返回 true 表示校验通过）
        if (value == null || value.isEmpty()) {
            return true;
        }
        // 有值时校验长度范围
        int length = value.length();
        if (length < min || length > max) {
            return false;
        }
        // 如果配置了正则表达式，则校验正则
        if (pattern != null) {
            return pattern.matcher(value).matches();
        }
        return true;
    }
}
