package com.jiwu.api.user.controller;

import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import com.jiwu.api.common.util.service.IPUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.*;
import com.jiwu.api.user.service.UserService;
import com.jiwu.api.common.main.event.user.UserLogoutEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.TimeUnit;


/**
 * 用户模块/登录注册模块
 */
@Slf4j
@Tag(name = "用户模块/登录注册模块", description = "用户模块")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 登陆注册模块
     */
    @Operation(summary = "登录-密码", tags = {"登录注册模块"})
    @PostMapping(value = "/login/pwd")
    @PortFlowControl(limit = 50, timeUnit = TimeUnit.MINUTES, time = 5, errorMessage = "系统繁忙，请5分钟后再试！")
    Result<String> toLoginByPwd(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        return Result.ok(userService.toUserLoginByPwd(loginDTO.getUsername(), loginDTO.getPassword(), 0, request));
    }

    @Operation(summary = "登录-密码(全用户通用)", tags = {"登录注册模块"})
    @PostMapping(value = "/login/pwd/all")
    @PortFlowControl(limit = 50, timeUnit = TimeUnit.MINUTES, time = 5, errorMessage = "系统繁忙，请5分钟后再试！")
    Result<String> toLoginByPwdAll(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        return Result.ok(userService.toUserLoginByPwd(loginDTO.getUsername(), loginDTO.getPassword(), null, request));
    }

    @Operation(summary = "登录-手机", tags = {"登录注册模块"})
    @PostMapping(value = "/login/phone")
    @PortFlowControl(limit = 4, timeUnit = TimeUnit.DAYS, time = 1, errorMessage = "您的手机额度今日不足！")
    Result<String> toLoginPhoneByCode(@Valid @RequestBody LoginPhoneCodeDTO loginPhoneCodeDTO, HttpServletRequest request) {
        return Result.ok(userService.toUserLoginByPhoneCode(loginPhoneCodeDTO.getPhone(), loginPhoneCodeDTO.getCode(), request));
    }


    @Operation(summary = "登录-邮箱", tags = {"登录注册模块"})
    @PostMapping(value = "/login/email")
    @PortFlowControl(limit = 10, timeUnit = TimeUnit.DAYS, time = 1, errorMessage = "您的邮箱额度今日不足！")
    Result<String> toLoginEmailByCode(@Valid @RequestBody LoginEmailCodeDTO loginEmailCodeDTO, HttpServletRequest request) {
        return Result.ok(userService.toUserLoginByEmailCode(loginEmailCodeDTO.getEmail(), loginEmailCodeDTO.getCode(), request));
    }


    @GetMapping(value = "/login/code/{key}")
    @Operation(summary = "登录 - 获取验证码", tags = {"登录注册模块"})
    @Parameter(name = "key", description = "手机号/邮箱", in = ParameterIn.PATH)
    @Parameter(name = "type", description = "类型：0手机号/ 1邮箱", in = ParameterIn.QUERY)
    @PortFlowControl(limit = 5, timeUnit = TimeUnit.MINUTES, time = 5, errorMessage = "繁忙，今日达到发送次数限制！")
    Result<Boolean> getLoginCode(@PathVariable String key, @RequestParam Integer type) {
        if (type == 0) {// 手机号
            return Result.ok(userService.getLoginCodeByPhone(key));
        } else {// 邮箱
            return Result.ok(userService.getLoginCodeByEmail(key));
        }
    }

    @Operation(summary = "登录-退出登录（当前）", tags = {"登录注册模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping(value = "/exit")
    Result<Boolean> toLogout(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        return Result.ok(userService.logoutOne(request.getAttribute(UserConstant.USER_ID_KEY).toString(), request));
    }


    @Operation(summary = "登录-退出登录（所有）", tags = {"登录注册模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping(value = "/exit/all")
    Result<Object> toLogoutAll(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        UserTokenDTO dto = new UserTokenDTO().setId(userId).setUa(request.getHeader(UserConstant.USER_AGENT_KEY));
        // 发布退出登录事件，由 chat 模块监听并处理 WebSocket 断开
        applicationEventPublisher.publishEvent(new UserLogoutEvent(this, dto, Boolean.TRUE));
        return userService.logoutAll(request.getAttribute(UserConstant.USER_ID_KEY).toString(), IPUtil.getIpAddress(request));
    }

    /*************************** 注册相关（注册、验证码） **************************/
    @Operation(summary = "注册", tags = {"登录注册模块"})
    @PostMapping(value = "/register")
    @PortFlowControl(limit = 3, timeUnit = TimeUnit.DAYS, time = 1, errorMessage = "您的注册额度今日不足！")
    Result<String> toRegister(@Valid @RequestBody UserRegisterDTO dto, HttpServletRequest request) {
        return Result.ok(userService.toRegister(dto, request));
    }

    @Operation(summary = "注册（快速免密）", tags = {"登录注册模块"})
    @PostMapping(value = "/register/v2")
    @PortFlowControl(limit = 3, timeUnit = TimeUnit.DAYS, time = 1, errorMessage = "您的注册额度今日不足！")
    Result<String> toRegisterV2(@Valid @RequestBody UserRegisterV2DTO dto, HttpServletRequest request) {
        return Result.ok(userService.toRegisterV2(dto, request));
    }

    @GetMapping(value = "/register/code/{key}")
    @Operation(summary = "获取注册验证码", tags = {"登录注册模块"})
    @Parameter(name = "key", description = "手机号/邮箱", in = ParameterIn.PATH)
    @Parameter(name = "type", description = "类型：0手机号/ 1邮箱", in = ParameterIn.QUERY)
    @PortFlowControl(limit = 3, timeUnit = TimeUnit.DAYS, time = 1, errorMessage = "本机每日注册数受限！")
    Result<Object> getRegisterCode(@PathVariable String key, @RequestParam Integer type) {
        if (type == 0) {// 手机号
            return Result.ok(userService.getRegisterCodeByPhone(key));
        } else if (type == 1) {// 邮箱
            return Result.ok(userService.getRegisterCodeByEmail(key));
        } else {
            return Result.fail("获取失败，参数错误！");
        }
    }


}
