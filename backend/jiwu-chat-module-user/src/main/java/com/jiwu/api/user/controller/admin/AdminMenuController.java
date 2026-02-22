package com.jiwu.api.user.controller.admin;

import com.jiwu.api.common.main.pojo.sys.Menu;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertMenuDTO;
import com.jiwu.api.user.common.dto.SelectMenuListDTO;
import com.jiwu.api.user.common.dto.UpdateMenuDTO;
import com.jiwu.api.user.common.vo.MenuTreeVO;
import com.jiwu.api.user.service.AdminUserMenuService;
import com.jiwu.api.common.main.dto.common.IdsList;
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
 * /用户模块/菜单管理
 */
@Slf4j
@Tag(name = "管理员模块", description = "菜单管理")
@RestController
@RequestMapping("/admin/user/menu")
public class AdminMenuController {

    @Autowired
    private AdminUserMenuService adminUserMenuService;


    @Operation(summary = "获取菜单列表（树）", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/tree")
    @ReqPermission(name = "获取菜单列表（树）", intro = "获取菜单列表（树）", expression = "admin:user:menu:tree:view")
    Result<List<MenuTreeVO>> getMenuTree(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return adminUserMenuService.getMenuTree();
    }


    @Operation(summary = "获取菜单列表（列表）", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("/list")
//    @ReqPermission(name = "获取菜单列表（列表）", intro = "获取菜单列表（列表）", expression = "admin:user:menu:list:view")
    Result<List<Menu>> getMenuList(
            @Valid @RequestBody SelectMenuListDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return adminUserMenuService.getMenuList(dto);
    }

    @Operation(summary = "获取用户菜单列表", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/list")
    Result<List<Menu>> getMenuList(
            HttpServletRequest request,
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String adminId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return adminUserMenuService.getMenuList(adminId);
    }


    @Operation(summary = "添加菜单", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PostMapping("")
    @ReqPermission(name = "添加菜单", intro = "添加菜单(单条添加)", expression = "admin:user:menu:add")
    Result<Integer> addMenuPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                HttpServletRequest request,
                                @Valid @RequestBody InsertMenuDTO dto) {
        return adminUserMenuService.addMenuPage(dto);
    }

    @Operation(summary = "修改菜单", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("{mid}")
    @ReqPermission(name = "修改菜单", intro = "修改菜单(单条添加)", expression = "admin:user:menu:{mid}:edit")
    Result<Integer> updateMenuPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                   HttpServletRequest request,
                                   @Valid @RequestBody UpdateMenuDTO dto,
                                   @PathVariable String mid) {
        return adminUserMenuService.updateMenuPage(mid, dto);
    }

    @Operation(summary = "删除菜单（批量）", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @DeleteMapping("")
    @ReqPermission(name = "删除菜单（批量）", intro = "删除菜单批量", expression = "admin:user:menu:batchDel")
    Result<Integer> batchDelMenuPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                     @Valid @RequestBody IdsList dto) {
        return adminUserMenuService.batchDelMenuPage(dto.getIds());
    }


    @Operation(summary = "关联角色菜单", tags = {"菜单模块"})
    @Parameter(name = "token", description = "管理员 token", required = true, in = ParameterIn.HEADER)
    @PutMapping("/bind/{roleId}")
    @ReqPermission(name = "关联角色菜单", intro = "添加、修改关联用户角色（管理员）", expression = "admin:user:menu:bind:{userId}:edit")
    Result<Integer> bindRoleMenu(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                 HttpServletRequest request,
                                 @Valid @RequestBody IdsList dto,
                                 @Parameter(description = "角色id") @PathVariable String roleId) {
        String adminId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return adminUserMenuService.bindRoleMenu(roleId, dto.getIds(), adminId);
    }

}
