package com.jiwu.api.user.controller.admin;

import com.jiwu.api.common.main.pojo.pay.RechargeCombo;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.main.dto.common.IdsList;
import com.jiwu.api.user.common.dto.RechargeComboDTO;
import com.jiwu.api.user.common.dto.UpdateRechargeComboDTO;
import com.jiwu.api.user.service.UserWalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 管理员模块
 * #钱包管理
 *
 * @className: AdminWalletController
 * @author: Kiwi23333
 * @description: 钱包、充值、套餐模块
 * @date: 2023/5/5 15:08
 */
@Slf4j
@Tag(name = "钱包管理")
@RestController
@RequestMapping("/admin/wallet")
public class AdminWalletController {
    @Autowired
    UserWalletService userWalletService;

    @Operation(summary = "获取充值套餐列表", tags = {"钱包模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/combo/list")
    @ReqPermission(name = "获取充值套餐列表", intro = "获取充值套餐列表", expression = "admin:wallet:combo:list:view")
    Result<List<RechargeCombo>> getRechargeCombo(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(userWalletService.getAllRechargeCombo());
    }

    @PostMapping("/combo/one")
    @Operation(summary = "添加充值套餐", tags = {"钱包模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @ReqPermission(name = "添加充值套餐", intro = "添加充值套餐", expression = "admin:wallet:combo:one:add")
    Result<Object> addRechargeCombo(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                    @Valid @RequestBody RechargeComboDTO dto) {
        userWalletService.addRechargeCombo(dto);
        return Result.ok();
    }

    @PutMapping("/combo/{id}")
    @Operation(summary = "修改充值套餐", tags = {"钱包模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @ReqPermission(name = "修改充值套餐", intro = "修改充值套餐", expression = "admin:wallet:combo:{id}:edit")
    Result<Object> updateRechargeCombo(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                       @Valid @RequestBody UpdateRechargeComboDTO dto,
                                       @PathVariable Integer id) {
        userWalletService.updateRechargeCombo(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除充值套餐（单个）", tags = {"钱包模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @ReqPermission(name = "删除充值套餐（单个）", intro = "删除充值套餐（单个）", expression = "admin:wallet:combo:one:{id}:del")
    @DeleteMapping("/combo/one/{id}")
    Result<Integer> delRechargeCombo(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @PathVariable Integer id) {
        return Result.ok(userWalletService.delRechargeCombo(id));
    }

    @DeleteMapping("/combo/some")
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @Operation(summary = "删除充值套餐（批量）", tags = {"钱包模块"})
    @ReqPermission(name = "删除充值套餐（批量）", intro = "删除充值套餐（批量）", expression = "admin:wallet:combo:some:batchDel")
    Result<Integer> delRechargeCombo(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @RequestBody IdsList list) {
        return Result.ok(userWalletService.batchDelRechargeCombo(list.getIds()));
    }
}
