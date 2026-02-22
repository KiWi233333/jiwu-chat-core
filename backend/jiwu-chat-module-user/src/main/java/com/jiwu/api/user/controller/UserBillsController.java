package com.jiwu.api.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.pay.UserBills;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.SelectBillsDTO;
import com.jiwu.api.common.main.dto.bills.BillsTimeTotalDTO;
import com.jiwu.api.common.main.dto.bills.BillsTotalDTO;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;
import com.jiwu.api.user.common.vo.BillsTotalVO;
import com.jiwu.api.user.service.UserBillsService;
import com.jiwu.api.common.constant.JwtConstant;
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
 * 账单模块控制器
 *
 * @className: UserWalletController
 * @author: Kiwi23333
 * @description: 账单模块
 * @date: 2023/4/30 22:27
 */
@Slf4j
@Tag(name = "用户模块", description = "账单模块")
@RestController
@RequestMapping("/user/bills")
public class UserBillsController {


    @Autowired
    UserBillsService billsService;

    @Operation(summary = "分页获取账单", tags = {"账单模块"})
    @Parameter(name = "Authorization", description = "用户token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/list/{page}/{size}")
//    @ReqPermission(name = "分页获取账单", intro = "用户分页获取账单", expression = "user:bills:list:page:size:view")
    Result<IPage<UserBills>> getBillsListByPage(@Parameter(description = "页码") @PathVariable int page,
                                                @Parameter(description = "每页个数") @PathVariable int size,
                                                @Valid @RequestBody SelectBillsDTO dto,
                                                @RequestHeader(JwtConstant.HEADER_NAME) String token,
                                                HttpServletRequest
                                                        request) {
        String userId = String.valueOf(request.getAttribute(UserConstant.USER_ID_KEY));
        IPage<UserBills> bills = billsService.getBillsByDto(userId, dto, page, size);
        return Result.ok("获取成功", bills);
    }

    @Operation(summary = "获取账单统计（详细）", tags = {"账单模块"})
    @Parameter(name = "Authorization", description = "用户token", required = true, in = ParameterIn.HEADER)
//    @ReqPermission(name = "获取账单统计（详细）",intro = "用户获取账单统计详细信息", expression = "user:bills:total:detail:view")
    @PostMapping("/total/detail")
    Result<BillsTotalVO> getBillsTotalInfo(
            @RequestHeader(JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody BillsTotalDTO dto,
            HttpServletRequest request) {
        String userId = String.valueOf(request.getAttribute(UserConstant.USER_ID_KEY));

        try {
            BillsTotalVO total = billsService.getBillsTotalInfo(userId, dto);
            return Result.ok("获取成功", total);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "获取账单统计（时间）", tags = {"账单模块"})
    @Parameter(name = "Authorization", description = "用户token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/total")
    Result<List<BillsTimeTotalVO>> getBillsTimeTotal(
            @RequestHeader(JwtConstant.HEADER_NAME) String token,
            @Valid @RequestBody BillsTimeTotalDTO dto,
            HttpServletRequest request) {
        String userId = String.valueOf(request.getAttribute(UserConstant.USER_ID_KEY));
        List<BillsTimeTotalVO> total = billsService.getBillsTotal(userId, dto);
        return Result.ok("获取成功", total);
    }

}
