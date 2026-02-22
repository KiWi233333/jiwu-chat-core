package com.jiwu.api.user.service;

import com.jiwu.api.common.main.pojo.sys.UserAddress;
import com.jiwu.api.user.common.dto.UserAddressDTO;

import java.util.*;
/**
 * 收货地址业务层
 *
 * @className: UserWalletService
 * @author: Kiwi23333
 * @description: 收货地址的增删查改
 * @date: 2023/4/30 15:49
 */
public interface UserAddressService {

    /**
     * 获取用户地址
     *
     * @param userId 用户id
     * @return List<UserAddress>
     */
     List<UserAddress> getUserAddressByUserId(String userId);
    /**
     * 添加收货地址
     *
     * @param dto    参数
     * @param userId 用户id
     */
     void addUserAddress(UserAddressDTO dto, String userId);

    /**
     * 修改收货地址
     *
     * @param dto    参数
     * @param id     地址id
     * @param userId 用户id
     */
     void updateAddressById(UserAddressDTO dto, String id, String userId);

    /**
     * 删除收货地址
     *
     * @param id     地址id
     * @param userId 用户id
     */
     void deleteAddressById(String id, String userId);

    /**
     * 修改是否默认地址
     *
     * @param id        地址id
     * @param userId    用户id
     * @param isDefault 是否默认
     * @return Boolean
     */
     Boolean updateAddressDefault(String id, String userId, Integer isDefault);



    /**
     * 批量删除地址
     *
     * @param ids ids
     * @param userId userId
     * @return 影响条数
     */
    Integer deleteAddressByIds(List<String> ids, String userId);
}
