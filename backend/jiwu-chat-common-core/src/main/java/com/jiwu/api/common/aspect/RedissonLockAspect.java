package com.jiwu.api.common.aspect;

import com.jiwu.api.common.annotation.RedissonLock;
import com.jiwu.api.common.util.common.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;

/**
 * Redisson分布式锁切面
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class RedissonLockAspect {

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String key = SpElUtils.parse(redissonLock.key(), method, joinPoint.getArgs());
        String lockKey = redissonLock.prefix() + key;

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            if (redissonLock.waitTime() > 0) {
                isLocked = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), redissonLock.unit());
            } else {
                lock.lock(redissonLock.leaseTime(), redissonLock.unit());
                isLocked = true;
            }

            if (isLocked) {
                return joinPoint.proceed();
            } else {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
