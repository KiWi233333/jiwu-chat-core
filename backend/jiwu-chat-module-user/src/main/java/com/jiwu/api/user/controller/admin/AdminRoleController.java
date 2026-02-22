package com.jiwu.api.user.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertRoleDTO;
import com.jiwu.api.user.common.dto.SelectPageRoleDTO;
import com.jiwu.api.user.common.dto.UpdateRoleDTO;
import com.jiwu.api.user.common.dto.UserRoleBindDTO;
import com.jiwu.api.user.common.vo.RoleTreeVO;
import com.jiwu.api.common.main.dto.common.IdsList;
import com.jiwu.api.user.service.AdminUserPermissionService;
import com.jiwu.api.user.service.AdminUserRoleService;
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
 * /用户模块/角色管理
 */
@Slf4j
@Tag(name = "管理员模块", description = "角色管理")
@RestController
@RequestMapping("/admin/user/role")
public class AdminRoleController {

    @Autowired
    AdminUserRoleService adminUserRoleService;
    @Autowired
    AdminUserPermissionService adminUserPermissionService;

    @Operation(summary = "获取角色列表（分页）", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/list/{page}/{size}")
    @ReqPermission(name = "获取角色列表（分页）", intro = "获取角色列表（分页）", expression = "admin:user:role:list:{page}:{size}:view")
    Result<IPage<Role>> getRolePage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                    @Parameter(description = "页码") @PathVariable int page,
                                    @Parameter(description = "每页个数") @PathVariable int size,
                                    @Valid @RequestBody SelectPageRoleDTO dto) {
        return adminUserRoleService.getRolePage(page, size, dto);
    }

    @Operation(summary = "获取角色列表（树）", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/tree")
    @ReqPermission(name = "获取角色列表（树）", intro = "获取角色列表（树）", expression = "admin:user:role:tree:{page}:{size}:view")
    Result<List<RoleTreeVO>> getRoleTree(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return adminUserRoleService.getRoleTree();
    }


    @Operation(summary = "获取用户角色列表", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/codes/{userId}")
    @ReqPermission(name = "获取用户角色列表", intro = "获取用户角色列表（code）", expression = "admin:user:role:codes:{userId}:view")
    Result<List<Role>> getRolePage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                     @Parameter(description = "用户id") @PathVariable String userId) {
        return Result.ok(adminUserRoleService.selectUserRoleList(userId));
    }


    @Operation(summary = "关联用户角色（管理员）", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/bind/{userId}")
    @ReqPermission(name = "关联用户角色（管理员）", intro = "添加、修改关联用户角色（管理员）", expression = "admin:user:role:bind:{userId}:edit")
    Result<Integer> addUserRoleBind(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                    HttpServletRequest request,
                                    @Valid @RequestBody UserRoleBindDTO dto,
                                    @Parameter(description = "用户id") @PathVariable String userId) {
        String adminId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return adminUserRoleService.addUserRoleBind(userId, dto.getRoleIds(), adminId);
    }


    @Operation(summary = "添加角色（单条）", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("")
    @ReqPermission(name = "添加角色（单条）", intro = "添加角色单条添加", expression = "admin:user:role:add")
    Result<Integer> addRolePage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                HttpServletRequest request,
                                @Valid @RequestBody InsertRoleDTO dto) {
        String adminId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return adminUserRoleService.addRolePage(adminId, dto);
    }

    @Operation(summary = "删除角色（批量）", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping("")
    @ReqPermission(name = "删除角色（批量）", intro = "删除角色批量", expression = "admin:user:role:batchDel")
    Result<Integer> batchDelRolePage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                     @Valid @RequestBody IdsList dto) {
        return adminUserRoleService.batchDelRolePage(dto.getIds());
    }

    @Operation(summary = "修改角色", tags = {"角色模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/{id}")
    @ReqPermission(name = "修改角色", intro = "修改角色信息", expression = "admin:user:role:edit")
    Result<Integer> updateRolePage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                   @Valid @RequestBody UpdateRoleDTO dto,
                                   HttpServletRequest request,
                                   @Parameter(description = "角色id") @PathVariable String id) {
        String adminId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return adminUserRoleService.updateRolePage(adminId, id, dto);
    }

}
