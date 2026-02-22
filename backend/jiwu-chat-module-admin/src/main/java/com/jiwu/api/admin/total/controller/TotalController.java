package com.jiwu.api.admin.total.controller;

import com.jiwu.api.admin.total.service.TotalService;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.main.dto.total.GroupTimeTotalDTO;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;
import com.jiwu.api.common.main.vo.total.IndexTotalVO;
import com.jiwu.api.common.util.service.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "系统管理", description = "管理员模块/统计管理")
@RequestMapping("/admin/total")
public class TotalController {

    @Autowired
    TotalService totalService;

    @Operation(summary = "获取首页概览统计", tags = {"统计模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/main")
    @ReqPermission(name = "获取首页概览统计（管理员主页）", intro = "获取首页概览统计（管理员主页）", expression = "admin:total:main:view")
    Result<IndexTotalVO> getHomeTotal(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(totalService.getIndexTotal());
    }

    @PostMapping("/bills/list")
    @Operation(summary = "获取账单统计列表", tags = {"统计模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "管理员token", required = true, in = ParameterIn.HEADER)
    @ReqPermission(name = "获取账单统计列表（管理员主页）", intro = "获取账单统计列表（管理员主页）", expression = "admin:total:bills:list:view")
    Result<List<BillsTimeTotalVO>> getBillsTotalList(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                                     @Valid @RequestBody GroupTimeTotalDTO dto) {
        return Result.ok(totalService.getBillsTotalList(dto));
    }
}
