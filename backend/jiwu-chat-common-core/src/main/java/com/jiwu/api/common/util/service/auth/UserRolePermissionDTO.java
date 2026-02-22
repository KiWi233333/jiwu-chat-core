package com.jiwu.api.common.util.service.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户token内部存储信息类
 * 存储用户角色权限
 *
 * @className: UserTokenDto
 * @author: Author作者
 * @description: 存储用户角色权限
 * @date: 2023/4/13 1:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserRolePermissionDTO {
    /**
     * 用户id
     */
    private String id;

    /**
     * 角色id
     */
    private String roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色编码
     */
    private String roleCode;
    /**
     * 角色类型 0用户 1管理员 2客服
     */
    private Integer roleType;


    /**
     * 权限id
     */
    private String permissionId;
    /**
     * 权限名称
     */
    private String permissionName;
    /**
     * 权限编码
     */
    private String permissionCode;
    /**
     * 权限类型
     */
    private Integer permissionType;

    private String permissionUrl;
}
