package com.jiwu.api.common.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * SpiceBaseMapper
 *
 * @className: SpliceBaseMapper
 * @author: Kiwi23333
 * @description: 封装自己的base-mapper,包含批量操作
 * @date: 2023/5/5 21:28
 */
public interface BatchBaseMapper<T> extends BaseMapper<T>{

    /**
     * 批量插入
     * {@link com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn}
     *
     * @param entityList 要插入的数据
     * @return 成功插入的数据条数
     */
    int insertBatchSomeColumn(List<T> entityList);
}
