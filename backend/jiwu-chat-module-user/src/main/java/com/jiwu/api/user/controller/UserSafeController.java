package com.jiwu.api.user.controller;

import com.jiwu.api.common.config.interceptor.UserAgentVO;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.DeleteSafeDTO;
import com.jiwu.api.user.service.UserSafeService;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户模块
 * #基本信息模块
 *
 * @className: UserInfoController
 * @author: Kiwi23333
 * @description: 基本信息模块
 * @date: 2023/5/1 2:09
 */
@Slf4j
@Tag(name = "用户模块", description = "账号安全和设备模块")
@RestController
@RequestMapping("/user")
public class UserSafeController {
    /*************************** 账号安全和设备 **************************/
    @Resource
    private UserSafeService userService;

    /**
     * 获取登录设备
     */
    @Operation(summary = "获取登录设备", tags = {"账号安全和设备模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @GetMapping(value = "/device")
    Result<List<UserAgentVO>> getUserLoginDevice(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                                 HttpServletRequest request) {
        String userId = (String) request.getAttribute(UserConstant.USER_ID_KEY);
        List<UserAgentVO> devices = userService.getUserLoginDevice(userId, request);
        return Result.ok("获取成功", devices);
    }

    @Operation(summary = "下线指定设备", tags = {"账号安全和设备模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping(value = "/device")
    Result<Integer> toUserOffline(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                  @RequestBody @Valid DeleteSafeDTO dto,
                                  HttpServletRequest request) {
        String userId = (String) request.getAttribute(UserConstant.USER_ID_KEY);
        Integer count = userService.toUserOfflineByOne(userId, dto);
        return Result.ok("下线成功！", count);
    }

}
