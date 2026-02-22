package com.jiwu.api.user.config.interceptor;

import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.IPUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import com.jiwu.api.user.service.AdminUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 频控拦截器
 *
 * @className: PortFlowInterceptor
 * @author: Kiwi23333
 * @description:
 * @date: 2023/4/29 1:47
 */
@Slf4j
@Component
@CrossOrigin
public class PortFlowInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AdminUserRoleService roleService;


    /**
     * 处理请求之前被调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断是否为HandlerMethod
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            if (!method.isAnnotationPresent(PortFlowControl.class)) {
                return true;
            }
            PortFlowControl flowControl = method.getAnnotation(PortFlowControl.class);
            // 判空
            if (flowControl == null) {
                return true;
            }
            // 超级管理员不受限制
            final UserTokenDTO userTokenDTO = RequestHolderUtil.get();
            if (userTokenDTO != null) {
                List<String> roleCodes = roleService.selectUserRole(userTokenDTO.getId());
                if (roleCodes != null && roleCodes.contains("SUPER_ADMIN")) {// TODO: 待修改为枚举常量
                    return true;
                }
            }
            // 1、设置redis
            int limit = flowControl.limit();// 次数
            long time = flowControl.time();// 秒数
            TimeUnit timeUnit = flowControl.timeUnit();// timeUnit
            // 2、redis key 存储类型
            String key = "";
            // 1）基于IP+接口+地址方式
            if (flowControl.keyType() == PortFlowControl.KeyType.IP) {
                key = IPUtil.getIpAddress(request) + request.getRequestURI();
            }
            // 检查屏控限制
            if (inc(key, time, timeUnit) > limit) {
                throw new BusinessException(ResultStatus.BUSY_ERR.getCode(), flowControl.errorMessage());
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }


    private static final String LUA_INCR_EXPIRE =
            "local key,ttl=KEYS[1],ARGV[1] " +
                    "if redis.call('EXISTS',key)==0 then" +
                    "  redis.call('SETEX',key,ttl,1)" +
                    "  return 1 " +
                    "else " +
                    "  return tonumber(redis.call('INCR',key)) " +
                    "end";

    /**
     * redis 执行脚本
     *
     * @param key      key
     * @param time     时间
     * @param timeUnit 时间单位
     * @return Long
     */
    private Long inc(String key, long time, TimeUnit timeUnit) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_INCR_EXPIRE, Long.class);
        return stringRedisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(timeUnit.toSeconds(time)));
    }

}
