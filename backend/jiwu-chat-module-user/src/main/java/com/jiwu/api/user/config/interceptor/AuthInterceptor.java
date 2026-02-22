package com.jiwu.api.user.config.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.annotation.IgnoreAuth;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.util.service.auth.JWTUtil;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import com.jiwu.api.user.service.AdminUserPermissionService;
import com.jiwu.api.user.service.AdminUserRoleService;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 身份验证拦截器
 *
 * @className: TokenInterceptor
 * @author: Kiwi23333
 * @description: token验证拦截器
 * @date: 2023/4/29 1:47
 */
@Slf4j
@Component
@CrossOrigin
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtil<String, UserTokenDTO> redisUtil;

    @Resource
    private AdminUserRoleService roleService;

    @Resource
    private AdminUserPermissionService permissionService;

    // @Value("${ai-device.allowed-key}")
    // private String AI_DEVICE_ALLOW_UA_CHECK_KEY;

    @Value("${mcp.server.allowed-key}")
    private String MCP_SERVER_UA_CHECK_KEY;

    @Value("${system.auth.enable-ua-check:true}")
    private boolean enableUaCheck;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull Object handler) throws Exception {
        return checkToken(request, response, handler);
    }

    /**
     * 验证用户
     *
     * @param request  请求
     * @param response 响应
     * @return boolean
     */
    private boolean checkToken(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        // 拦截器取到请求先进行判断，如果是OPTIONS请求，则放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // 支持跨域
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods",
                    "GET,POST,PUT,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            return true;
        }

        // 检查是否有 @IgnoreAuth 注解，如果有则跳过身份验证
        if (hasIgnoreAuthAnnotation(handler)) {
            log.info("接口包含 @IgnoreAuth 注解，跳过身份验证：{}", request.getServletPath());
            return true;
        }

        String secret = request.getHeader(JwtConstant.ALLOW_SECRET_KEY_NAME);
        String token = request.getHeader(JwtConstant.HEADER_NAME);

        // 获取 token 并校验 JWT
        UserTokenDTO userTokenDTO;
        // 1) token 为空
        if (StringUtils.isBlank(token)) {
            response.getWriter().write(JacksonUtil.toJSON(Result.fail("验证错误，您还未登录！")));
            response.sendError(401, "token不能为空!");
            return false; // Token验证失败，请求中止
        }

        // 2）token 不为空
        try {
            // 1、验证用户
            userTokenDTO = JWTUtil.getTokenInfoByToken(token);
            // 请求拦截权限设定 (无权限拦截)
            boolean checkPermission = checkPermission(userTokenDTO, request, handler);
            if (!checkPermission) {
                response.getWriter().write(JacksonUtil.toJSON(Result.fail(ResultStatus.PERMISSION_ERR, "暂无权限，无法访问！")));
                log.info("暂无权限，无法访问！{}", userTokenDTO.getId());
                return false;
            }
            // 1、redis获取当前token是否有效、验证登录设备是否存在
            // 获取对应userAgent
            String userAgent = request.getHeader(UserConstant.USER_AGENT_KEY);
            if (enableUaCheck
                    && !redisUtil.hExists(UserConstant.USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(), userAgent)
                    && !MCP_SERVER_UA_CHECK_KEY.equals(secret)) { // 跳过 UA 验证
                response.getWriter().write(JacksonUtil.toJSON(Result.fail(ResultStatus.TOKEN_DEVICE_ERR)));
                log.info("身份验证错误，登录设备有误！");
                return false;
            }

            // 2、验证是否过期
            long time = redisUtil.getExpire(UserConstant.USER_REFRESH_TOKEN_KEY + userTokenDTO.getId());
            if (time < 0) {
                response.getWriter()
                        .write(JacksonUtil.toJSON(Result.fail(ResultStatus.TOKEN_EXPIRED_ERR, "登录已过期，请重新登陆！")));
                log.info("身份已过期, {}", userTokenDTO.getId());
                return false;
            } else {
                // a）redis_token续期
                redisUtil.expire(UserConstant.USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(),
                        JwtConstant.REDIS_TOKEN_TIME, TimeUnit.MINUTES);
                // 将用户id放入头部 用于业务使用
                request.setAttribute("userId", userTokenDTO.getId());
            }
            // 头部存放信息
            RequestHolderUtil.set(userTokenDTO);
            return true;
        } catch (TokenExpiredException e1) {
            response.setStatus(403);
            response.getWriter().write(JacksonUtil.toJSON(Result.fail(ResultStatus.TOKEN_EXPIRED_ERR)));
            return false;
        }
        // catch (JWTDecodeException e) {
        // response.getWriter().write(JacksonUtil.toJSON(Result.fail(ResultStatus.TOKEN_ERR,
        // "身份校验失败，请重新登陆！")));
        // return false;
        // }
        catch (IOException e) {
            // 3、登录错误
            response.setStatus(403);
            response.getWriter().write(JacksonUtil.toJSON(Result.fail(ResultStatus.TOKEN_ERR, "身份校验失败，请重新登陆！")));
            return false;
        }
    }

    /**
     * 检查是否有忽略身份验证注解
     *
     * @param handler 处理器
     * @return boolean
     */
    private boolean hasIgnoreAuthAnnotation(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            // 检查方法上是否有 @IgnoreAuth 注解
            if (method.isAnnotationPresent(IgnoreAuth.class)) {
                return true;
            }

            // 检查类上是否有 @IgnoreAuth 注解
            Class<?> controllerClass = handlerMethod.getBeanType();
            if (controllerClass.isAnnotationPresent(IgnoreAuth.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证身份
     *
     * @param dto UserTokenDTO
     * @return boolean
     */
    private boolean checkPermission(UserTokenDTO dto, HttpServletRequest request, Object handler) {

        // 判断是否为HandlerMethod
        if (handler instanceof HandlerMethod handlerMethod) {
            try {
                Method method = handlerMethod.getMethod();

                // 1、获取用户权限-角色 (优化缓存)
                if (roleService == null) {
                    log.warn("无法获取 RoleService,跳过权限检查");
                    return true;
                }

                List<String> roleCodes = roleService.selectUserRole(dto.getId());

                Set<String> permissionHasSet = new HashSet<>();
                if (permissionService != null) {
                    for (String roleCode : roleCodes) {
                        permissionHasSet.addAll(permissionService.selectUserPermission(roleCode));
                    }
                }

                // 基本判断
                String url = request.getServletPath();
                log.info("用户id:{}，{}请求url：{}", dto.getId(), request.getMethod(), url);
                // 放行超级管理员
                if (roleCodes.contains("SUPER_ADMIN")) {
                    return true;
                }
                // 是否存在注解（ReqPermission）
                if (!method.isAnnotationPresent(ReqPermission.class)) {
                    return true;
                }
                ReqPermission permission = method.getAnnotation(ReqPermission.class);
                // 判空
                if (permission == null) {
                    return true;
                }
                // 1、接口要求的权限code
                String requirePermission = permission.expression();// 权限code
                // 比对
                return permissionHasSet.contains(requirePermission);
            } catch (Exception e) {
                log.error("权限检查失败: {}", e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

}
