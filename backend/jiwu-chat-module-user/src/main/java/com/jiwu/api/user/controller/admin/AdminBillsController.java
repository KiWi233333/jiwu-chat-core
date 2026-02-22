package com.jiwu.api.user.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.pay.UserBills;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.SelectAllBillsDTO;
import com.jiwu.api.user.service.UserBillsService;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@Tag(name = "账单管理")
@RestController
@RequestMapping("/admin/bills")
public class AdminBillsController {

    @Autowired
    UserBillsService userBillsService;

    @Operation(summary = "获取账单列表（分页）", tags = { "订单管理" })
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/list/{page}/{size}")
    @ReqPermission(name = "获取账单列表（分页）（管理员）", intro = "管理员获取账单列表（分页）", expression = "admin:bills:list:{page}:{size}:view")
    Result<IPage<UserBills>> getOrderPageByDTO(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Parameter(description = "页码") @PathVariable int page,
            @Parameter(description = "每页个数") @PathVariable int size,
            @Valid @RequestBody SelectAllBillsDTO dto) {
        return Result.ok(userBillsService.getBillsByDto(dto.getUserId(), SelectAllBillsDTO.toBillsDTO(dto), page, size));
    }

}
