package com.jiwu.api.common.main.mapper.sys;

import com.github.yulichang.base.MPJBaseMapper;
import com.jiwu.api.common.main.mapper.BatchBaseMapper;
import com.jiwu.api.common.main.pojo.sys.UserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BatchBaseMapper<UserRole>, MPJBaseMapper<UserRole> {

}
