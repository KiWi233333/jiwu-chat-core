package com.jiwu.api.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertRoleDTO;
import com.jiwu.api.user.common.dto.SelectPageRoleDTO;
import com.jiwu.api.user.common.dto.UpdateRoleDTO;
import com.jiwu.api.user.common.vo.RoleTreeVO;

import java.util.List;

/**
 * 用户角色表
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
public interface AdminUserRoleService {
    /**
     * 添加用户用户角色关联
     *
     * @param userId 用户id
     * @return int
     */
    int addUserRoleCustomer(String userId);

    /**
     * 获取用户所有角色
     *
     * @param userId 用户userId
     * @return 角色 code列表
     */
    List<String> selectUserRole(String userId);
    /**
     * 获取用户所有角色列表
     *
     * @param userId 用户userId
     * @return 角色 code列表
     */
    List<Role> selectUserRoleList(String userId);

    /**
     * 获取角色列表（分页）
     *
     * @param page 页
     * @param size 数
     * @param dto  筛选dto
     * @return 分页数据Result<IPage < Role>>
     */
    Result<IPage<Role>> getRolePage(int page, int size, SelectPageRoleDTO dto);
    /**
     * 获取角色列表（分页树）
     *
     * @return 分页数据Result<IPage < Role>>
     */
    Result<List<RoleTreeVO>> getRoleTree();


    /**
     * 添加角色
     *
     * @param adminId 添加者权限
     * @param dto     参数
     * @return 行数
     */
    Result<Integer> addRolePage(String adminId, InsertRoleDTO dto);


    /**
     * 批量删除角色（包括关联权限）
     * @param ids 删除id列表
     * @return 删除行数
     */
    Result<Integer> batchDelRolePage(List<String> ids);

    /**
     * 修改角色
     *
     * @param adminId 角色id
     * @param id 更新id
     * @param dto 修改参数
     * @return 行数
     */
    Result<Integer> updateRolePage(String adminId, String id, UpdateRoleDTO dto);

    /**
     * 用户绑定角色
     * @param userId 用户id
     * @param ids 角色id列表
     * @param adminId 创建人id
     * @return 绑定数
     */
    Result<Integer> addUserRoleBind(String userId, List<String> ids, String adminId);

}
