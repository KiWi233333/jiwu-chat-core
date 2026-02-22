package com.jiwu.api.common.main.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.mapper.BatchBaseMapper;
import com.jiwu.api.common.main.pojo.sys.RoleMenu;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu>, MPJBaseMapper<RoleMenu>, BatchBaseMapper<RoleMenu> {
}
