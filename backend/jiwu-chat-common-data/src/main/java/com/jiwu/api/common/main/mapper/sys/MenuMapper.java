package com.jiwu.api.common.main.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.mapper.BatchBaseMapper;
import com.jiwu.api.common.main.pojo.sys.Menu;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuMapper extends BaseMapper<Menu>, BatchBaseMapper<Menu> {
}
