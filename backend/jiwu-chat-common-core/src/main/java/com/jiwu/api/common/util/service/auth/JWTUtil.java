package com.jiwu.api.common.util.service.auth;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jiwu.api.common.constant.JwtConstant;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.util.service.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JWt工具类
 *
 * @className: JWTUtil
 * @author: Kiwi2333
 * @description: TODO描述
 * @date: 2023/4/13 1:17
 */
@Slf4j
@Component
public class JWTUtil {

    static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RedisUtil redisUtil;

    /**
     * 1、生成Token
     *
     * @param obj 加密对象
     * @return token
     */
    public static String createToken(UserTokenDTO obj) {
        try {
            // 设置头部信息
            Map<String, Object> header = new HashMap<>(2);
            header.put("alg", "HS256");
            header.put("Type", "Jwt");
            Date now = new Date();
//            obj.setDateTime(now.getTime());
            String userJson = objectMapper.writeValueAsString(obj);
            return JWT.create().withHeader(header).withNotBefore(now) // 生效时间
                    .withIssuedAt(now) // 签发时间
                    .withIssuer(JwtConstant.ISSUER) // 用于说明该JWT是由谁签发的
                    .withSubject(JwtConstant.SUBJECT_OBJ) // 用于说明该JWT面向的对象
                    .withAudience(JwtConstant.SAVE_OBJ_KEY) // 用于说明该JWT发送给的用户
                    // 有效期
                    .withExpiresAt(Date.from(ZonedDateTime.now().plusMinutes(JwtConstant.TOKEN_TIME).toInstant())) // 数字类型，说明该JWT过期的时间  //ZonedDateTime.now().plusMinutes()此方法基于此日期时间返回添加了分钟数的ZonedDateTime    // 随机jwtId
                    .withJWTId(UUID.randomUUID().toString()) // 说明标明JWT的唯一ID
                    // 存储地址
                    .withClaim(JwtConstant.SAVE_OBJ_KEY, userJson) // 存入user信息
                    // 算法
                    .sign(Algorithm.HMAC256(JwtConstant.SECRET_KEY));
        } catch (Exception e) {
            log.error("生成token失败！\ncreate token occur error, error is:{}", e);
            return null;
        }
    }

    @Async
    public String renewToken(String ua, UserTokenDTO dto) {
        Long days = redisUtil.getExpire(UserConstant.USER_REFRESH_TOKEN_KEY + dto.getId(), TimeUnit.DAYS);
        if (days == -2) {// 不存在key
            return null;
        }
        // 续期
        if (days < JwtConstant.TOKEN_REFRESH_DAYS) {
            redisUtil.expire(UserConstant.USER_REFRESH_TOKEN_KEY + dto.getId(), JwtConstant.TOKEN_TIME, TimeUnit.MINUTES);
            return createToken(dto);
        }
        return null;
    }


    /**
     * 检验token是否正确
     *
     * @param token
     * @return JsonParser
     */
    public static JsonParser checkToken(String token) throws IOException {
        Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(JwtConstant.ISSUER) // 用于说明该JWT是由谁签发的
                .withSubject(JwtConstant.SUBJECT_OBJ) // 用于说明该JWT面向的对象
                .withAudience(JwtConstant.SAVE_OBJ_KEY) // 用于说明该JWT发送给的用户
                .build();
        DecodedJWT jwt = verifier.verify(token);
        String tokenInfo = jwt.getClaim(JwtConstant.SAVE_OBJ_KEY).asString();// 存入user
        return new JsonFactory().createParser(tokenInfo);
    }


    /**
     * 解析token的信息
     *
     * @param jsonParser cs
     * @return UserTokenDTO
     */
    public static UserRolePermissionDTO getTokenInfo(JsonParser jsonParser) throws IOException {
        return objectMapper.readValue(jsonParser, UserRolePermissionDTO.class);
    }


    /**
     * 验证并获取用户存储信息
     *
     * @param token
     * @return UserTokenDTO
     */
    public static UserTokenDTO getTokenInfoByToken(String token) throws IOException {
        Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(JwtConstant.ISSUER) // 用于说明该JWT是由谁签发的
                .withSubject(JwtConstant.SUBJECT_OBJ) // 用于说明该JWT面向的对象
                .withAudience(JwtConstant.SAVE_OBJ_KEY) // 用于说明该JWT发送给的用户
                .build();
        DecodedJWT jwt = verifier.verify(token);
        String tokenInfo = jwt.getClaim(JwtConstant.SAVE_OBJ_KEY).asString();// 存入user
        return objectMapper.readValue(new JsonFactory().createParser(tokenInfo), UserTokenDTO.class);
    }

    public static UserTokenDTO getTokenInfoByHeader(HttpServletRequest request) {
        String token = request.getHeader(JwtConstant.HEADER_NAME);
        try {
            return JWTUtil.getTokenInfoByToken(token);
        } catch (Exception e) {
            log.warn("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }


}
