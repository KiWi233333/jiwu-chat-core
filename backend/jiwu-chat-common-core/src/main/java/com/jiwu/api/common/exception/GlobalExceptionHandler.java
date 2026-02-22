package com.jiwu.api.common.exception;

import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.service.Result;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Set;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * validation参数校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Object> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException e) {
        log.info("参数校验异常：{}", e.getMessage());
        return Result.fail(ResultStatus.PARAM_ERR.getCode(), Objects.requireNonNull(e.getBindingResult().getFieldError().getDefaultMessage()));
    }


    /**
     * validation参数校验异常 ConstraintViolationException
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<Object> methodArgumentNotValidExceptionExceptionHandler(ConstraintViolationException e) {
        log.info("参数校验异常：{}", e.getMessage());
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        if (!violations.isEmpty()) {
            ConstraintViolation<?> item = violations.iterator().next();
            return Result.fail(ResultStatus.PARAM_ERR.getCode(), item.getMessageTemplate());
        }
        return Result.fail(ResultStatus.PARAM_ERR.getCode(), "参数校验失败！");
    }


    /**
     * validation参数校验异常
     */
    @ExceptionHandler(value = BindException.class)
    public Result<Object> bindException(BindException e) {
        log.info("参数校验异常：{}", e.getMessage());
        String str = StringUtil.isNullOrEmpty(Objects.requireNonNull(e.getGlobalError()).getDefaultMessage()) ? e.getGlobalError().getDefaultMessage() : "参数校验失败！";
        return Result.fail(ResultStatus.PARAM_ERR.getCode(), str);
    }

    /**
     * 自定义校验异常（如参数校验等）
     */
    @ExceptionHandler(value = BusinessException.class)
    public Result<Object> businessExceptionHandler(BusinessException e) {
        log.warn("业务错误，business exception！The reason is：{}", e.getMessage(), e);
        return Result.fail(e.getErrorCode(), e.getMessage(), e.getData());
    }

}
