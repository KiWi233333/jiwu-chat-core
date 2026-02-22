package com.jiwu.api.user.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.sys.Permission;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertPermissionDTO;
import com.jiwu.api.user.common.dto.SelectPagePermissionDTO;
import com.jiwu.api.user.common.dto.SelectPermissionDTO;
import com.jiwu.api.user.common.dto.UpdatePermissionDTO;
import com.jiwu.api.user.common.vo.ValidPermissionVO;
import com.jiwu.api.common.main.dto.common.IdsList;
import com.jiwu.api.user.service.AdminUserPermissionService;
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
 * 管理员模块
 * #用户管理
 * #权限管理
 */
@Slf4j
@Tag(name = "管理员模块", description = "权限管理")
@RestController
@RequestMapping("/admin/user/permission")
public class AdminPermissionController {

    @Autowired
    AdminUserPermissionService adminUserPermissionService;

    @Operation(summary = "初始化全部权限（超级用户）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/init")
    @ReqPermission(name = "初始化全部权限（超级用户）", intro = "初始化全部权限（超级用户）", expression = "admin:user:permission:init:add")
    Result<List<Permission>> initPermission(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        List<Permission> list = adminUserPermissionService.initPermission();
        return Result.ok(list.isEmpty() ? "暂无更多权限接口！" : "添加成功，共添加" + list.size() + "权限", list);
    }

    @Operation(summary = "获取权限列表（分页）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/list/{page}/{size}")
    @ReqPermission(name = "获取权限列表（分页）", intro = "获取权限列表（分页）", expression = "admin:user:permission:list:{page}:{size}:view")
    Result<IPage<Permission>> getPermissionPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                                @Parameter(description = "页码") @PathVariable int page,
                                                @Parameter(description = "每页个数") @PathVariable int size,
                                                @Valid @RequestBody SelectPagePermissionDTO dto) {
        return Result.ok(adminUserPermissionService.getPermissionPage(page, size, dto));
    }


    @Operation(summary = "获取权限列表（未使用）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/list/valid")
    @ReqPermission(name = "获取权限列表（未使用）", intro = "获取权限list列表", expression = "admin:user:permission:list:valid:view")
    Result<List<ValidPermissionVO>> getPermissionValidList(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(adminUserPermissionService.getPermissionValidList());
    }

    @Operation(summary = "获取权限列表（有效）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/list/exist")
    @ReqPermission(name = "获取权限列表（有效）", intro = "获取有效已使用的权限列表（已存在、已使用）", expression = "admin:user:permission:list:exist:view")
    Result<List<Permission>> getPermissionExistList(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                                    @Valid @RequestBody SelectPermissionDTO dto) {
        return Result.ok(adminUserPermissionService.getPermissionExistList(dto));
    }


    @Operation(summary = "添加权限（单条）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("")
    @ReqPermission(name = "添加权限（单条）", intro = "添加权限单条添加", expression = "admin:user:permission:add")
    Result<Integer> addPermissionPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                      HttpServletRequest request,
                                      @Valid @RequestBody InsertPermissionDTO dto) {
        String adminId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return Result.ok("添加成功！", adminUserPermissionService.addPermissionPage(adminId, dto));
    }

    @Operation(summary = "修改权限", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/{id}")
    @ReqPermission(name = "修改权限", intro = "修改权限", expression = "admin:user:permission:{id}:edit")
    Result<Integer> updatePermissionPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                         @Parameter(description = "权限id") @PathVariable String id,
                                         @Valid @RequestBody UpdatePermissionDTO dto) {
        return Result.ok("修改成功！", adminUserPermissionService.updatePermissionPage(id, dto));
    }

    @Operation(summary = "删除权限（单条）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping("/{id}")
    @ReqPermission(name = "删除权限（单条）", intro = "删除权限（单条）", expression = "admin:user:permission:{id}:del")
    Result<Integer> delPermissionPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                      @Parameter(description = "权限id") @PathVariable String id) {
        return Result.ok("删除单条成功！",adminUserPermissionService.delPermissionPage(id));
    }

    @Operation(summary = "删除权限（批量）", tags = {"权限模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping("/some")
    @ReqPermission(name = "删除权限（批量）", intro = "删除权限的批量删除", expression = "admin:user:permission:some:batchDel")
    Result<Integer> delPermissionPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                      @Valid @RequestBody IdsList list) {
        return Result.ok("批量删除成功！",adminUserPermissionService.batchDelPermissionPage(list.getIds()));
    }


}
