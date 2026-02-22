package com.jiwu.api.common.aspect;

import com.jiwu.api.common.annotation.FlowControl;
import com.jiwu.api.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 频控注解 切面
 *
 * @className: FlowControlAspect
 * @author: Kiwi23333
 * @description: 频控注解 切面
 * @date: 2023/7/23 2:25
 */
@Aspect
@Slf4j
@Component
public class FlowControlAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LUA_INCR_EXPIRE =
            "local key,ttl=KEYS[1],ARGV[1] " +
                    "if redis.call('EXISTS',key)==0 then" +
                    "  redis.call('SETEX',key,ttl,1)" +
                    "  return 1 " +
                    "else " +
                    "  return tonumber(redis.call('INCR',key)) " +
                    "end";

    @Around("@annotation(flowControl)")
    public Object doFlowControl(ProceedingJoinPoint joinPoint, FlowControl flowControl) throws Throwable {
        String key="";
        switch (flowControl.target()) {
            case AUTO:
                key = flowControl.value() + joinPoint.getArgs()[0];
                break;
            case EL:
                key = flowControl.value();
                break;
        }
        log.info("@annotation(flowControl) 频控开启 {}", key);
        // 检查屏控限制
        if (inc(key, flowControl.duration(), flowControl.timeUnit()) <= flowControl.limit()) {
            return joinPoint.proceed(); // 执行原始方法
        } else {
            throw new BusinessException(flowControl.errorCode(), flowControl.errorMsg()); // 返回错误消息
        }
    }

    /**
     * redis 执行脚本
     *
     * @param key      key
     * @param time     时间
     * @param timeUnit 时间单位
     * @return Long
     */
    public Long inc(String key, long time, TimeUnit timeUnit) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_INCR_EXPIRE, Long.class);
        return stringRedisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(timeUnit.toSeconds(time)));
    }


}
