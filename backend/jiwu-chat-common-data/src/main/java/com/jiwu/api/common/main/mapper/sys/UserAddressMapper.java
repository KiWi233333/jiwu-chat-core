package com.jiwu.api.common.main.mapper.sys;

import com.jiwu.api.common.main.mapper.BatchBaseMapper;
import com.jiwu.api.common.main.pojo.sys.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址
 *
 * @className: UserAddressMapper
 * @author: Kiwi23333
 * @description: 用户收货地址
 * @date: 2023/5/16 13:16
 */
@Mapper
public interface UserAddressMapper  extends BatchBaseMapper<UserAddress> {
}
