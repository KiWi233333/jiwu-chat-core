package com.jiwu.api.user.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 权限dto
 *
 * @className: RoleDTO
 * @author: Kiwi23333
 * @description: 权限dto
 * @date: 2023/5/3 1:10
 */
@Data
@Accessors(chain = true)
public class PermissionDTO {
    /**
     * 权限id
     */
    private String id;
    /**
     * 权限名称
     */
    private String name;
    /**
     * 权限编码
     */
    private String code;
    /**
     * 路径
     */
    private String url;
    /**
     * 权限类型 0用户 1管理员 2客服
     */
    private Integer type;
}
