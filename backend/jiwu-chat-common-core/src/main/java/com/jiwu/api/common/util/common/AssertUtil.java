package com.jiwu.api.common.util.common;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import org.hibernate.validator.HibernateValidator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 校验工具类
 */
public class AssertUtil {

    /**
     * 校验到失败就结束
     */
    private static final Validator failFastValidator = Validation.byProvider(HibernateValidator.class)
            .configure()
            .failFast(true)
            .buildValidatorFactory().getValidator();

    /**
     * 全部校验
     */
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 注解验证参数(校验到失败就结束)
     *
     * @param obj
     */
    public static <T> void fastFailValidate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = failFastValidator.validate(obj);
        if (!constraintViolations.isEmpty()) {
            throwException(ResultStatus.PARAM_ERR, constraintViolations.iterator().next().getMessage());
        }
    }

    /**
     * 注解验证参数(全部校验,抛出异常)
     *
     * @param obj 参数
     */
    public static <T> void allCheckValidateThrow(T obj) {
        // 1. 注解校验
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        if (!constraintViolations.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            for (ConstraintViolation<T> violation : constraintViolations) {
                //拼接异常信息
                errorMsg.append(violation.getPropertyPath().toString()).append(":").append(violation.getMessage()).append(",");
            }
            //去掉最后一个逗号
            throwException(ResultStatus.PARAM_ERR, errorMsg.substring(0, errorMsg.length() - 1));
        }
    }


    /**
     * 注解验证参数(全部校验,返回异常信息集合)
     */
    public static <T> Map<String, String> allCheckValidate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        if (!constraintViolations.isEmpty()) {
            Map<String, String> errorMessages = new HashMap<>();
            for (ConstraintViolation<T> violation : constraintViolations) {
                errorMessages.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return errorMessages;
        }
        return new HashMap<>();
    }

    public static void isTrue(boolean expression, String msg) {
        if (!expression) {
            throwException(msg);
        }
    }


    public static void checkSecondWhenFirstTrue(boolean firstExp, boolean expression, String msg) {
        if (firstExp && !expression) {
            throwException(msg);
        }
    }

    public static void isTrue(boolean expression, ResultStatus status, String errorMsg) {
        if (!expression) {
            throwException(status, errorMsg);
        }
    }

    //如果是true，则抛异常
    public static void isFalse(boolean expression, String msg) {
        if (expression) {
            throwException(msg);
        }
    }

    //如果是true，则抛异常
    public static void isFalse(boolean expression, ResultStatus status, Object... args) {
        if (expression) {
            throwException(status, args);
        }
    }

    //如果不是非空对象，则抛异常
    public static void isNotEmpty(Object obj, String msg) {
        if (isEmpty(obj)) {
            throwException(msg);
        }
    }

    //如果不是非空对象，则抛异常
    public static void isNotBlank(String obj, String msg) {
        if (CharSequenceUtil.isBlank(obj)) {
            throwException(msg);
        }
    }

    //如果不是非空对象，则抛异常
    public static void isNotEmpty(Object obj, ResultStatus status, Object... args) {
        if (isEmpty(obj)) {
            throwException(status, args);
        }
    }

    //如果不是非空对象，则抛异常
    public static void isNotEmpty(Object obj, ResultStatus status, String errorMsg) {
        if (isEmpty(obj)) {
            throwException(status, errorMsg);
        }
    }

    //如果不是非空对象，则抛异常
    public static void isEmpty(Object obj, String msg) {
        if (!isEmpty(obj)) {
            throwException(msg);
        }
    }

    public static void equal(Object o1, Object o2, String msg) {
        if (!ObjectUtil.equal(o1, o2)) {
            throwException(msg);
        }
    }

    public static void equal(ResultStatus status, Object o1, Object o2, String msg) {
        if (!ObjectUtil.equal(o1, o2)) {
            throwException(status, msg);
        }
    }

    public static void notEqual(Object o1, Object o2, String msg) {
        if (ObjectUtil.equal(o1, o2)) {
            throwException(msg);
        }
    }

    public static void notEqual(ResultStatus status, Object o1, Object o2, String msg) {
        if (ObjectUtil.equal(o1, o2)) {
            throwException(msg);
        }
    }

    private static boolean isEmpty(Object obj) {
        return ObjectUtil.isEmpty(obj);
    }

    private static void throwException(String msg) {
        throwException(ResultStatus.DEFAULT_ERR, msg);
    }

    private static void throwException(ResultStatus status, Object... arg) {
        if (Objects.isNull(status)) {
            status = ResultStatus.DEFAULT_ERR;
        }
        throw new BusinessException(status.getCode(), status.getMessage());
    }

    private static void throwException(ResultStatus status, String msg) {
        if (Objects.isNull(status)) {
            status = ResultStatus.DEFAULT_ERR;
        }
        throw new BusinessException(status.getCode(), msg);
    }

}
