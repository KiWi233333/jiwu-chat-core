package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.main.mapper.sys.UserAddressMapper;
import com.jiwu.api.common.main.pojo.sys.UserAddress;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.user.common.dto.UserAddressDTO;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.user.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收货地址业务层
 *
 * @className: UserWalletService
 * @author: Kiwi23333
 * @description: 收货地址的增删查改
 * @date: 2023/4/30 15:49
 */
@Service
@Slf4j
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;

    /**
     * 获取用户地址
     *
     * @param userId 用户id
     * @return List<UserAddress>
     */
    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        List<UserAddress> addressList = new ArrayList<>();
        // 1、获取缓存
        Map<String, Object> map = redisUtil.hGetAll(UserConstant.USER_ADDRESS_PAGE_KEY + userId);
        if (!map.isEmpty()) {
            for (String key : map.keySet()) {
                addressList.add((UserAddress) map.get(key));
            }
            return addressList;
        }
        // 2、sql
        addressList = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)); // 调用Mapper接口方法进行分页查询
        // 3、缓存
        map = new HashMap<>();
        for (UserAddress address : addressList) {// 遍历添加map
            map.put(address.getId(), address);
        }
        redisUtil.hPutAll(UserConstant.USER_ADDRESS_PAGE_KEY + userId, map);
        return addressList;
    }

    /**
     * 添加收货地址
     *
     * @param dto    参数
     * @param userId 用户id
     */
    @Override
    @Transactional
    public void addUserAddress(UserAddressDTO dto, String userId) {
        UserAddress userAddress = UserAddressDTO.toUserAddress(dto).setUserId(userId);
        // 1、如果是默认地址,先取消其他默认地址
        if (dto.getIsDefault().equals(1) || dto.getIsDefault() == 1) {
            userAddressMapper.update(new UserAddress().setIsDefault(0), new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId));
        }
        // 2、添加
        if (userAddressMapper.insert(userAddress) < 0) {
            throw new BusinessException("添加失败！");
        }
        redisUtil.delete(UserConstant.USER_ADDRESS_PAGE_KEY + userId);
    }

    /**
     * 修改收货地址
     *
     * @param dto    参数
     * @param id     地址id
     * @param userId 用户id
     */
    public void updateAddressById(UserAddressDTO dto, String id, String userId) {
        UserAddress userAddress = UserAddressDTO.toUserAddress(dto)
                .setUserId(userId);
        LambdaQueryWrapper<UserAddress> qw = new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getId, id);
        // 1、如果是默认地址,先取消其他默认地址
        if (dto.getIsDefault().equals(1)) {
            int count = userAddressMapper.update(new UserAddress().setIsDefault(0), new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId));
            if (count == 0) {
                log.warn("修改旧地址出现问题!");
                throw new BusinessException("修改失败！");
            }
        }
        if (userAddressMapper.update(userAddress, qw) <= 0) {
            throw new BusinessException("修改失败！");
        }
        // 2、删除全部缓存
        redisUtil.delete(UserConstant.USER_ADDRESS_PAGE_KEY + userId);
    }

    /**
     * 删除收货地址
     *
     * @param id     地址id
     * @param userId 用户id
     */
    public void deleteAddressById(String id, String userId) {

        // 1、sql
        if (userAddressMapper.delete(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId).eq(UserAddress::getId, id)) <= 0) {
            throw new BusinessException("删除失败！");
        }
        // 2、删除缓存
        redisUtil.hDelete(UserConstant.USER_ADDRESS_PAGE_KEY + userId, id);
    }

    /**
     * 修改是否默认地址
     *
     * @param id        地址id
     * @param userId    用户id
     * @param isDefault 是否默认
     * @return Result
     */
    public Boolean updateAddressDefault(String id, String userId, Integer isDefault) {
        AssertUtil.isTrue(this.setIsDefault(id, userId, isDefault), "修改失败，请稍后再试！");
        return true;
    }


    // 修改用户列表
    private boolean setIsDefault(String id, String userId, Integer isDefault) {
        LambdaQueryWrapper<UserAddress> qw = new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId);
        if (isDefault.equals(1))
            userAddressMapper.update(new UserAddress().setIsDefault(0), qw);
        // 修改单条
        if (userAddressMapper.update(new UserAddress().setIsDefault(isDefault.equals(1) ? 1 : 0), qw.eq(UserAddress::getId, id)) <= 0)
            return false;
        redisUtil.delete(UserConstant.USER_ADDRESS_PAGE_KEY + userId);
        return true;
    }


    /**
     * 批量删除地址
     *
     * @param ids    地址id列表
     * @param userId 用户id
     * @return 影响条数
     */
    @Override
    public Integer deleteAddressByIds(List<String> ids, String userId) {
        int flag = userAddressMapper.deleteBatchIds(ids);
        if (flag <= 0) {
            throw new BusinessException("删除失败！");
        }
        return flag;
    }
}
