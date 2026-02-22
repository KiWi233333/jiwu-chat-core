package com.jiwu.api.common.main.mapper.sys;

import com.jiwu.api.common.main.mapper.BatchBaseMapper;
import com.jiwu.api.common.main.pojo.sys.RolePermission;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper extends MPJBaseMapper<RolePermission>, BatchBaseMapper<RolePermission> {
}
