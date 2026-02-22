package com.jiwu.api.user.service;

import com.jiwu.api.common.main.pojo.sys.Menu;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.*;
import com.jiwu.api.user.common.dto.InsertMenuDTO;
import com.jiwu.api.user.common.dto.SelectMenuListDTO;
import com.jiwu.api.user.common.dto.UpdateMenuDTO;
import com.jiwu.api.user.common.vo.MenuTreeVO;

import java.util.List;

/**
 * 用户角色表
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
public interface AdminUserMenuService {

    /**
     * 获取用户菜单树
     * @return 菜单列表
     */
    Result<List<MenuTreeVO>> getMenuTree();

    /**
     * 添加菜单
     * @param dto 参数
     * @return 影响行
     */
    Result<Integer> addMenuPage(InsertMenuDTO dto);

    /**
     * 删除菜单（批量）
     * @param ids id集合
     * @return 影响行
     */
    Result<Integer> batchDelMenuPage(List<String> ids);

    /**
     * 绑定角色-菜单
     * @param roleId 角色id
     * @param ids 绑定列表
     * @param adminId 创建人
     * @return 影响行
     */
    Result<Integer> bindRoleMenu(String roleId, List<String> ids, String adminId);

    /**
     * 获取菜单列表
     * @param dto 参数
     * @return 列表
     */
    Result<List<Menu>> getMenuList(SelectMenuListDTO dto);
    /**
     * 获取用户的菜单列表
     * @param userId 用户id
     * @return 列表
     */
    Result<List<Menu>> getMenuList(String userId);

    /**
     * 修改菜单
     * @param id 菜单id
     * @param dto 参数
     * @return 影响行数
     */
    Result<Integer> updateMenuPage(String id, UpdateMenuDTO dto);
}
