package com.jiwu.api.user.controller;

import com.jiwu.api.common.main.pojo.pay.RechargeCombo;
import com.jiwu.api.common.main.pojo.pay.UserWallet;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.WalletRechargeDTO;
import com.jiwu.api.user.service.UserWalletService;
import com.jiwu.api.common.constant.UserConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;


/**
 * 用户模块
 * <p>
 * 钱包模块控制器
 *
 * @className: UserWalletController
 * @author: Kiwi23333
 * @description: 钱包模块
 * @date: 2023/4/30 22:27
 */
@Slf4j
@Tag(name = "用户模块", description = "钱包模块")
@RestController
@RequestMapping("/user")
public class UserWalletController {

    @Autowired
    private UserWalletService userWalletService;


    @Operation(summary = "获取充值套餐", tags = {"钱包模块"})
    @GetMapping("/wallet/combo")
    Result<List<RechargeCombo>> getAllRechargeCombo() {
        return Result.ok("获取成功", userWalletService.getAllRechargeCombo());
    }


    @Operation(summary = "获取钱包信息", tags = {"钱包模块"})
    @Parameter(name = "Authorization", description = "用户token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/wallet")
    Result<UserWallet> getUserPurseInfo(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        return Result.ok("获取成功", userWalletService.getUserWalletById(String.valueOf(request.getAttribute(UserConstant.USER_ID_KEY))));
    }


    @Operation(summary = "钱包充值", tags = {"钱包模块"})
    @Parameter(name = "Authorization", description = "用户token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/wallet")
    Result<Void> toRechargeByCombo(@RequestHeader("Authorization") String token,
                             @Valid @RequestBody WalletRechargeDTO walletRechargeDTO, HttpServletRequest request) {

        try {
            userWalletService.toRechargeByUserId(walletRechargeDTO, String.valueOf(request.getAttribute(UserConstant.USER_ID_KEY)));
            return Result.ok("充值成功！", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }


}
