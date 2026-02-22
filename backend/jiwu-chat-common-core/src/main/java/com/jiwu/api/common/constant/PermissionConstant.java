package com.jiwu.api.common.constant;


import com.jiwu.api.common.util.service.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionConstant {
    public static final String SYS_USER_ROLE_MAP = "sys:user:role:map:";
    public static final String SYS_ALL_ROLE_PERMISSION_MAP = "sys:all:user:role:permission:map:";
    public static final String SYS_ALL_ROLE_MENU_MAP = "sys:all:user:role:menu:map:";

    @Autowired
    RedisUtil redisUtil;
    /*************  删除角色  *************/
    public Boolean delUserRoleMap() {
        return redisUtil.delete(SYS_USER_ROLE_MAP);
    }
    public Long delUserRoleMap(String userId) {
        return redisUtil.hDelete(SYS_USER_ROLE_MAP, userId);
    }


    /*************  删除角色->权限  *************/
    public boolean delPermissionMap() {
        return redisUtil.delete(SYS_ALL_ROLE_PERMISSION_MAP);
    }
    public Long delPermissionMap(String roleId) {
        return redisUtil.hDelete(SYS_ALL_ROLE_PERMISSION_MAP, roleId);
    }
    public Long delPermissionMapByRoleCode(String roleCode) {
        return redisUtil.hDelete(SYS_ALL_ROLE_PERMISSION_MAP, roleCode);
    }
    public Long delPermissionMap(List<String> roles) {
        return redisUtil.hDelete(SYS_ALL_ROLE_PERMISSION_MAP, roles);
    }


    /*************  删除角色->菜单 *************/
    public Boolean delUserMenuMap() {
        return redisUtil.delete(SYS_ALL_ROLE_MENU_MAP);
    }

    public Long delUserMenuMap(String roleId) {
        return redisUtil.hDelete(SYS_ALL_ROLE_MENU_MAP, roleId);
    }
    public Long delUserMenuMap(String roleId, String adminId) {
        this.delUserMenuMapByUId(adminId);
        return redisUtil.hDelete(SYS_ALL_ROLE_MENU_MAP, roleId);
    }

    public Long delUserMenuMapByUId(String userId) {
        return redisUtil.hDelete(SYS_ALL_ROLE_MENU_MAP, "userId:" + userId);
    }

    public boolean delAll() {
        return redisUtil.delete(SYS_ALL_ROLE_MENU_MAP) && redisUtil.delete(SYS_USER_ROLE_MAP);
    }
}

