package com.jiwu.api.user.controller;

import com.jiwu.api.common.main.dto.common.IdsList;
import com.jiwu.api.common.main.pojo.sys.UserAddress;
import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.*;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.user.common.vo.UserVO;
import com.jiwu.api.user.service.UserAddressService;
import com.jiwu.api.user.service.UserService;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
@Tag(name = "用户模块", description = "基本信息模块")
@RestController
@RequestMapping("/user")
public class UserInfoController {


    /* ************* 用户基本信息（增删查改）************ */
    @Resource
    private UserService userService;

    @Operation(summary = "获取用户信息", tags = {"用户基本信息模块"})
    @GetMapping("/info")
    Result<UserVO> getUserInfo(@RequestHeader(JwtConstant.HEADER_NAME) String toke) {
        // 获取拦截请求后获取的id
        return Result.ok(userService.getUserInfoById(String.valueOf(RequestHolderUtil.get().getId())));
    }

    @Operation(summary = "修改密码", tags = {"用户基本信息模块"})
    @PutMapping("/info/pwd")
    @ReqPermission(name = "修改密码(常规用户)", intro = "修改密码(常规用户)，预览用户不可使用", expression = "user:info:pwd:edit")
    Result<Boolean> updateUserPwd(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdatePwdDTO updatePwdDto) {
        // 处理验证错误
        if (updatePwdDto.getNewPassword().equals(updatePwdDto.getOldPassword())) return Result.fail("新旧密码一致");
        return Result.ok(userService.updatePwdByOldNewPwd(updatePwdDto, String.valueOf(RequestHolderUtil.get().getId())));
    }

    @Operation(summary = "修改密码(验证码校验)", tags = {"用户基本信息模块"})
    @Parameter(name = "type", description = "类型：0手机号/ 1邮箱", required = true, in = ParameterIn.PATH)
    @ReqPermission(name = "修改密码V2(常规用户)", intro = "修改密码V2(常规用户)，预览用户不可使用", expression = "user:info:pwd:{type}:edit")
    @PutMapping("/info/pwd/{type}")
    Result<Integer> updateUserPwdV2(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdateSecondPwdDTO dto, @PathVariable Integer type) {
        AssertUtil.isTrue(type == 0 || type == 1, "抱歉，参数错误！");
        return Result.ok(userService.updatePwdByCode(type, dto));
    }

    @Operation(summary = "修改基本信息", tags = {"用户基本信息模块"})
    @ReqPermission(name = "修改基本信息(常规用户)", intro = "修改基本信息(常规用户)，预览用户不可使用", expression = "user:info:edit")
    @PutMapping("/info")
    Result<Boolean> updateUserInfo(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        return Result.ok(userService.updateUserInfo(updateUserInfoDTO, String.valueOf(RequestHolderUtil.get().getId())));
    }

    @Operation(summary = "获取验证用户验证码(短信|邮箱)", tags = {"用户基本信息模块"})
    @ReqPermission(name = "修改密码V2(常规用户)", intro = "修改密码V2(常规用户)，预览用户不可使用", expression = "user:info:pwd:{type}:edit")
    @GetMapping(value = "/info/check/code/{key}")
    @Parameter(name = "key", description = "手机号/邮箱", in = ParameterIn.PATH)
    @Parameter(name = "type", description = "类型：0手机号/ 1邮箱", in = ParameterIn.QUERY)
    Result<Boolean> getCheckCode(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @PathVariable String key, @RequestParam Integer type) {
        return Result.ok(userService.sendCheckCode(key, type));
    }

    @Operation(summary = "验证用户验证码", tags = {"用户基本信息模块"})
    @ReqPermission(name = "验证用户获取验证码(常规用户)", intro = "验证用户获取验证码(常规用户)，用于修改密码等", expression = "user:info:check:code:get")
    @PostMapping(value = "/info/check/code")
    Result<Boolean> onCheckUserAuth(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @Valid @RequestBody CheckCodeDTO dto) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        return Result.ok(userService.onCheckCode(userId, dto.getType(), dto.getKey(), dto.getCode()));
    }

    // ---------------------------更换手机号|邮箱------------------------------------------
    @Operation(summary = "更换手机号", tags = {"用户基本信息模块"})
    @ReqPermission(name = "更换手机号(常规用户)", intro = "更换手机号(常规用户)，预览用户不可使用", expression = "user:info:phone:edit")
    @PutMapping("/info/phone")
    Result<Boolean> updateUserPhone(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdatePhoneDTO updatePhoneDTO) {
        // 业务
        return Result.ok(userService.updateUserPhone(updatePhoneDTO, String.valueOf(RequestHolderUtil.get().getId())));
    }

    @Operation(summary = "更换邮箱", tags = {"用户基本信息模块"})
    @ReqPermission(name = "更换邮箱(常规用户)", intro = "更换邮箱(常规用户)，预览用户不可使用", expression = "user:info:email:edit")
    @PutMapping("/info/email")
    Result<Boolean> updateUserEmail(
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody UpdateEmailDTO updateEmailDTO) {
        // 业务
        return Result.ok(userService.updateUserEmail(updateEmailDTO, String.valueOf(RequestHolderUtil.get().getId())));
    }

    @Operation(summary = "获取新手机/邮箱验证码", tags = {"用户基本信息模块"})
    @GetMapping(value = "/info/code/{key}")
    @Parameter(name = "key", description = "手机号/邮箱", in = ParameterIn.PATH)
    @Parameter(name = "type", description = "类型：0手机号/ 1邮箱", in = ParameterIn.QUERY)
    @ReqPermission(name = "获取新手机/邮箱验证码(常规用户)", intro = "获取新手机/邮箱验证码(常规用户)，预览用户不可使用", expression = "user:info:code:{key}:get")
    Result<Boolean> getUpdatePhoneOrEmailCode(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                              @PathVariable String key, @RequestParam Integer type) {
        return Result.ok(userService.sendUpdateCode(key, type));
    }


    @Operation(summary = "用户头像更改", tags = {"用户基本信息模块"})
    @Parameter(name = "file", description = "图片文件", in = ParameterIn.QUERY)
    @ReqPermission(name = "用户头像更改(常规用户)", intro = "用户头像更改(常规用户)，预览用户不可使用", expression = "user:info:avatar:edit")
    @PutMapping("/info/avatar")
    @PortFlowControl(limit = 5, time = 24, timeUnit = TimeUnit.HOURS, errorMessage = "请求频繁，每天仅允许修改5次头像！")
        // 每天仅允许修改5次头像
    Result<String> updateUserAvatar(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                    @RequestParam(name = "file") MultipartFile file) {
        return Result.ok(userService.updateUserAvatar(file, String.valueOf(RequestHolderUtil.get().getId())));
    }


    @Operation(summary = "验证-用户名是否存在", tags = {"用户基本信息模块"})
    @Parameter(name = "username", description = "用户名", in = ParameterIn.QUERY)
    @GetMapping("/exist")
    Result<Object> checkUserExisted(@RequestParam String username) {
        return userService.checkUserIsExist(username);
    }


    /* ********************** 用户地址 ***************************** */
    @Resource
    private UserAddressService userAddressService;

    @Operation(summary = "获取用户地址", tags = {"用户地址模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/address")
    Result<List<UserAddress>> getUserAddressPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        // 查询
        List<UserAddress> addresses = userAddressService.getUserAddressByUserId(userId);
        return Result.ok("获取成功！", addresses);
    }

    @Operation(summary = "添加收货地址", tags = {"用户地址模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/address")
    Result<Object> addUserAddress(@Valid @RequestBody UserAddressDTO userAddressDTO,
                                  @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        // 添加
        userAddressService.addUserAddress(userAddressDTO, userId);
        return Result.ok("添加成功！", null);
    }

    @Operation(summary = "修改收货地址", tags = {"用户地址模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/address/{id}")
    Result<Object> updateAddressById(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                     @Valid @RequestBody UserAddressDTO userAddressDTO,
                                     @PathVariable String id) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        // 修改
        userAddressService.updateAddressById(userAddressDTO, id, userId);
        return Result.ok("修改成功！", null);
    }

    @Operation(summary = "删除收货地址（单个）", tags = {"用户地址模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping("/address/one/{id}")
    Result<Object> deleteAddressById(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                     @PathVariable String id) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        // 删除
        userAddressService.deleteAddressById(id, userId);
        return Result.ok("删除成功！", null);
    }

    @Operation(summary = "删除收货地址（批量）", tags = {"用户地址模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping("/address/some")
    Result<Integer> deleteAddressByIds(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                      @Valid @RequestBody IdsList idsList) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        // 删除
        Integer count = userAddressService.deleteAddressByIds(idsList.getIds(), userId);
        return Result.ok("删除成功！", count);
    }

    @Operation(summary = "设为默认地址", tags = {"用户地址模块"})
    @Parameter(name = "token", description = "用户 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/address/default/{id}")
    Result<Boolean> setAddressDefault(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                      @RequestParam int isDefault,
                                      @PathVariable String id) {
        String userId = String.valueOf(RequestHolderUtil.get().getId());
        if (isDefault != 0 && isDefault != 1) {
            return Result.fail("默认值为0或1");
        }
        // 修改默认地址
        return Result.ok(userAddressService.updateAddressDefault(id, userId, isDefault));
    }


}
