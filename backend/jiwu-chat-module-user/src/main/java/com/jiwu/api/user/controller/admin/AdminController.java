package com.jiwu.api.user.controller.admin;

import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.LoginDTO;
import com.jiwu.api.user.common.dto.UpdateAvatarDTO;
import com.jiwu.api.user.common.dto.UpdatePwdDTO;
import com.jiwu.api.user.service.AdminService;
import com.jiwu.api.user.common.dto.UpdateUserInfoDTO;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import com.jiwu.api.user.common.vo.UserVO;
import com.jiwu.api.user.service.UserService;
import com.jiwu.api.common.main.event.user.UserLogoutEvent;
import org.springframework.context.ApplicationEventPublisher;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 管理员模块/个人管理
 */
@Slf4j
@Tag(name = "管理员模块/个人管理", description = "个人管理")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private AdminService adminService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private static final List<Integer> ADMIN_LOGIN_TYPES = Arrays.asList(UserType.ADMIN.getCode(), UserType.SERVICE.getCode(), UserType.ROBOT.getCode());

    /**
     * 登陆模块
     */
    @Operation(summary = "登录-密码", tags = {"个人管理"})
    @PostMapping(value = "/login/pwd")
    Result<String> toLoginByPwd(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        // 后台类用户
        return Result.ok(userService.toUserLoginByPwdAndTypes(loginDTO.getUsername(), loginDTO.getPassword(), ADMIN_LOGIN_TYPES, request));
    }

    @Operation(summary = "获取管理员信息", tags = {"个人管理"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/user/info")
    Result<UserVO> getUserInfo(@RequestHeader(JwtConstant.HEADER_NAME) String token) {
        // 获取拦截请求后获取的id
        return Result.ok(userService.getUserInfoById(RequestHolderUtil.get().getId()));
    }

    @Operation(summary = "管理员头像更改", tags = {"个人管理"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/info/avatar")
    @PortFlowControl(limit = 10, time = 24, timeUnit = TimeUnit.HOURS, errorMessage = "请求频繁，每天仅允许修改10次头像！")
        // 每天仅允许修改5次头像
    Result<String> updateUserAvatar(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                    @Valid @RequestBody UpdateAvatarDTO dto,
                                    HttpServletRequest request) {
        return Result.ok(adminService.updateAvatar(RequestHolderUtil.get().getId(), dto.getFileName()));
    }


    @Operation(summary = "修改密码（管理员）", tags = {"个人管理"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/info/pwd")
    @PortFlowControl(limit = 10, time = 24, timeUnit = TimeUnit.HOURS, errorMessage = "请求频繁，每天仅允许修改10次密码！")
    Result<Boolean> updateUserPwd(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                  @Valid @RequestBody UpdatePwdDTO dto,
                                  HttpServletRequest request) {
        return Result.ok(adminService.updateUserPwd(RequestHolderUtil.get().getId(), dto));
    }

    @Operation(summary = "修改个人信息", tags = {"个人管理"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/info")
    Result<Boolean> updateInfo(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdateUserInfoDTO dto,
            HttpServletRequest request) {
        return Result.ok(userService.updateUserInfo(dto, request.getAttribute(UserConstant.USER_ID_KEY).toString()));
    }


    @Operation(summary = "登录-退出登录", tags = {"登录模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping(value = "/exit")
    Result<Boolean> toLogout(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        UserTokenDTO dto = new UserTokenDTO().setId(userId).setUa(request.getHeader(UserConstant.USER_AGENT_KEY));
        // 发布退出登录事件，由 chat 模块监听并处理 WebSocket 断开
        applicationEventPublisher.publishEvent(new UserLogoutEvent(this, dto, Boolean.FALSE));
        return Result.ok(userService.logoutOne(userId, request));
    }


}
