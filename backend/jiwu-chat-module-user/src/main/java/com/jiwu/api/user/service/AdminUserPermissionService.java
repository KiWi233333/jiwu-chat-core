package com.jiwu.api.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.sys.Permission;
import com.jiwu.api.user.common.dto.InsertPermissionDTO;
import com.jiwu.api.user.common.dto.SelectPagePermissionDTO;
import com.jiwu.api.user.common.dto.SelectPermissionDTO;
import com.jiwu.api.user.common.dto.UpdatePermissionDTO;
import com.jiwu.api.user.common.vo.ValidPermissionVO;

import java.util.List;

/**
 * 权限
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
public interface AdminUserPermissionService {
    /**
     * 获取用户所有权限code
     *
     * @param roleCode roleCode
     * @return List<String>
     */
    List<String> selectUserPermission(String roleCode);

    /**
     * 项目初始化全部权限
     */
    List<Permission> initPermission();


    /**
     * 获取权限列表（分页）
     *
     * @param dto  筛选dto
     * @param page 页
     * @param size 数
     * @return 分页数据
     */
    IPage<Permission> getPermissionPage(int page, int size, SelectPagePermissionDTO dto);


    /**
     * 修改权限
     *
     * @param id  权限id
     * @param dto 参数
     * @return 成功条数
     */
    Integer updatePermissionPage(String id, UpdatePermissionDTO dto);

    /**
     * 添加权限
     *
     * @param adminId 管理员id（添加者）
     * @param dto     参数
     * @return 影响行数
     */
    Integer addPermissionPage(String adminId, InsertPermissionDTO dto);

    /**
     * 删除权限
     *
     * @param id 权限id
     * @return 删除行数
     */
    Integer delPermissionPage(String id);

    /**
     * 获取权限code列表
     *
     * @param dto 查询参数
     * @return 列表
     */
    List<Permission> getPermissionExistList(SelectPermissionDTO dto);

    /**
     * 获取可用的权限codes（新增、修改权限）
     *
     * @return
     */
    List<ValidPermissionVO> getPermissionValidList();

    /**
     * 删除权限（批量）
     * @param ids ids未使用
     * @return  删除条数
     */
    Integer batchDelPermissionPage(List<String> ids);
}
