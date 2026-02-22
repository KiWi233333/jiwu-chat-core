//package com.example.back_jiwuquang_api.core.constant.filters;
//
//import com.auth0.jwt.exceptions.TokenExpiredException;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import com.example.back_jiwuquang_api.core.constant.JwtConstant;
//import com.example.back_jiwuquang_api.dto.sys.UserTokenDTO;
//import com.example.back_jiwuquang_api.util.JWTUtil;
//import com.example.back_jiwuquang_api.util.JacksonUtil;
//import com.example.back_jiwuquang_api.util.RedisUtil;
//import com.example.back_jiwuquang_api.util.Result;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import static com.example.back_jiwuquang_api.core.constant.JwtConstant.REDIS_TOKEN_TIME;
//import static com.example.back_jiwuquang_api.core.constant.UserConstant.USER_REFRESH_TOKEN_KEY;
//
///**
// * 自定义一个过滤器，该过滤器用来对所有请求进行验证，判断请求的header中是否携带token，如果携带token则验证token的合法性。通过解析token获取用户名username，再从缓存中获取该username的用户信息，将用户信息存到SecurityContextHolder中。
// */
//@Slf4j
//@Component
//public class AuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    RedisUtil redisUtil;
//
//    private String[] ignoreUrl = {
//            "/user/login/**",
//            "/user/register/**",
//            "/goods/**",
//    };
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        //获取token
//        String token = request.getHeader(JwtConstant.HEADER_NAME);
//        response.setContentType("application/json;charset=UTF-8");
//        // 1、token
//
//        log.info("-----------------身份验证中------------------");
//        // 2、获取token
//        UserTokenDTO userTokenDTO;
//        // 1) token 为空
//        if (!StringUtils.isNotBlank(token)) {
//            response.getWriter().write(
//                    JacksonUtil.toJSON(Result.fail("验证错误，您还未登录！"))
//            );
//            response.sendError(401, "token不能为空!");
//            return; // Token验证失败，请求中止
//        }
//        // 2）token 不为空
//        try {
//            // 1、验证用户
//            // 获取对应userAgent
//            String userAgent = request.getHeader("User-Agent");
//            userTokenDTO = JWTUtil.getTokenInfoByToken(token);
//            //  1)redis获取当前token是否有效
//            Map<String, Object> map = redisUtil.hGetAll(USER_REFRESH_TOKEN_KEY + userTokenDTO.getId());
//            if (map.isEmpty()) {
//                redisUtil.hPut(USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(), userAgent, token);
//            }
//            //  2）验证登录设备是否存在
//            boolean flag = false;
//            for (String ua : map.keySet()) {
//                if (map.get(ua).equals(token) && ua.equals(userAgent)) {
//                    flag = true;
//                }
//            }
//            if (!flag) {
//                response.getWriter().write(JacksonUtil.toJSON(Result.fail("身份验证错误，登录设备有误！")));
//                log.info("身份验证错误，登录设备有误！");
//                return;
//            }
//            //  3）验证是否过期
//            if (redisUtil.hGet(USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(), userAgent) == null) {
//                response.getWriter().write(JacksonUtil.toJSON(Result.fail("身份验证错误，登录设备有误！")));
//                log.info("身份已全过期！");
//                return;
//            }
//            // 2、redis_token续期
//            redisUtil.expire(USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(), REDIS_TOKEN_TIME, TimeUnit.MINUTES);
//            // 将用户id放入头部 用于业务使用
//            request.setAttribute("userId", userTokenDTO.getId());
//            // return true; 放行
//            filterChain.doFilter(request, response);
//        } catch (TokenExpiredException e1) {
//            log.info("身份已过期 {}", e1.getMessage());
//            response.getWriter().write(JacksonUtil.toJSON(Result.fail("身份已过期，请重新登陆！")));
//        } catch (IOException e) {
//            // 3、身份错误
//            log.info("身份错误 {}", e.getMessage());
//            response.getWriter().write(JacksonUtil.toJSON(Result.fail("身份验证失败！")));
//        }
//    }
//
//
//}
