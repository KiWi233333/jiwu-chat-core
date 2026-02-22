package com.jiwu.api.user.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertAdminUserDTO;
import com.jiwu.api.user.common.dto.UpdateNewPwdDTO;
import com.jiwu.api.user.common.dto.UpdateUserAllInfoDTO;
import com.jiwu.api.user.common.dto.UserInfoPageDTO;
import com.jiwu.api.user.service.AdminUserService;
import com.jiwu.api.user.common.vo.UserVO;
import com.jiwu.api.user.service.UserService;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


/**
 * 管理员模块/用户管理
 * #用户管理
 */
@Slf4j
@Tag(name = "管理员模块/用户管理", description = "用户管理")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    AdminUserService adminUserService;
    @Resource
    UserService userService;

    @Operation(summary = "分页获取用户列表", tags = {"用户模块"})
    @PostMapping(value = "/{page}/{size}")
    @ReqPermission(name = "分页获取用户列表（管理员）", intro = "分页获取用户列表（管理员）", expression = "admin:user:{page}:{size}:view")
    Result<Page<User>> getUserInfoPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                       @Parameter(description = "页码") @PathVariable Integer page,
                                       @Parameter(description = "每页个数") @PathVariable Integer size,
                                       @Valid @RequestBody UserInfoPageDTO userInfoPageDTO) {
        Page<User> infoPage = adminUserService.getUserInfoPage(page, size, userInfoPageDTO);
        if (infoPage.getRecords() != null) {
            for (User p : infoPage.getRecords()) {
                p.setPassword(null);
            }
        }
        return Result.ok(infoPage);
    }


    @Operation(summary = "添加管理员用户", tags = {"用户模块"})
    @PostMapping("/new")
    @ReqPermission(name = "添加管理员用户（管理员）", intro = "添加管理员用户（管理员）", expression = "admin:user:info:{userId}:add")
    Result<Integer> addAdminUser(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody InsertAdminUserDTO insertAdminUserDTO,
            HttpServletRequest request) {
        return adminUserService.addAdminUser(insertAdminUserDTO);
    }


    @Operation(summary = "获取用户信息", tags = {"用户模块"})
    @GetMapping(value = "/{userId}")
    @ReqPermission(name = "获取用户信息（管理员）", intro = "获取用户信息（管理员）", expression = "admin:user:{userId}:view")
    Result<UserVO> getUserInfoPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                   @Parameter(description = "用户id") @PathVariable String userId) {
        return Result.ok(userService.getUserInfoById(userId));
    }

    @PutMapping("/info/{userId}")
    @Operation(summary = "修改用户信息", tags = {"用户模块"})
    @ReqPermission(name = "修改用户信息（管理员）", intro = "修改用户信息（管理员）", expression = "admin:user:info:{userId}:edit")
    Result<Integer> updateUserInfo(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdateUserAllInfoDTO updateUserAllInfoDTO,
            @PathVariable String userId,
            HttpServletRequest request) {
        return Result.ok(userService.updateUserAllInfo(updateUserAllInfoDTO, userId));
    }

    @PutMapping("/info/pwd/{userId}")
    @Operation(summary = "修改用户密码", tags = {"用户模块"})
    @ReqPermission(name = "修改用户密码（管理员）", intro = "修改用户密码（管理员）- 部分情况下使用", expression = "admin:user:info:pwd:{userId}:edit")
    Result<Integer> updateUserPwd(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdateNewPwdDTO dto,
            @PathVariable String userId) {
        return Result.ok(userService.updateUserPwdByAdmin(userId, dto));
    }

    @Operation(summary = "用户禁用", tags = {"用户模块"})
    @DeleteMapping(value = "/disable/{userId}")
    @ReqPermission(name = "用户禁用（管理员操作）", intro = "用户禁用（管理员操作）", expression = "admin:user:disable:{userId}:del")
    Result<Integer> toUserDisable(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @PathVariable String userId, @Parameter(description = "是否禁用") @RequestParam Integer disable) {
        return adminUserService.toUserDisableToggle(userId, disable);
    }

    @Operation(summary = "用户强制下线", tags = {"用户模块"})
    @ReqPermission(name = "用户强制下线（管理员操作）", intro = "用户强制下线（管理员操作）", expression = "admin:user:logout:{userId}:del")
    @DeleteMapping(value = "/logout/{userId}")
    Result<Integer> toUserLogout(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @PathVariable String userId) {
        return adminUserService.loginOutById(userId);
    }

}
