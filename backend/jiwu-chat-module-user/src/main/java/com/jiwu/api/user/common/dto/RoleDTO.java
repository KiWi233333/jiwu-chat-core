package com.jiwu.api.user.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色dto
 *
 * @className: RoleDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/3 1:10
 */
@Data
@Accessors(chain = true)
public class RoleDTO {
    /**
     * 角色id
     */
    private String id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色编码
     */
    private String code;
    /**
     * 角色类型 0用户 1管理员 2客服
     */
    private Integer type;
}
