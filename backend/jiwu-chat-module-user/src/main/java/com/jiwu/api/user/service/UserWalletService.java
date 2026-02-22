package com.jiwu.api.user.service;

import com.jiwu.api.common.main.pojo.pay.RechargeCombo;
import com.jiwu.api.common.main.pojo.pay.UserWallet;
import com.jiwu.api.user.common.dto.RechargeComboDTO;
import com.jiwu.api.user.common.dto.UpdateRechargeComboDTO;
import com.jiwu.api.user.common.dto.WalletRechargeDTO;
import com.jiwu.api.user.common.dto.UpdateWalletDTO;

import java.util.List;

/**
 * 钱包业务层
 *
 * @className: UserWalletService
 * @author: Kiwi23333
 * @description: 钱包的增删查改
 * @date: 2023/4/30 15:49
 */
public interface UserWalletService {

    /**
     * 初始化钱包（插入一条用户数据）
     *
     * @param userId 用户id
     * @return 返回受影响的函数
     */
    int initUserWallet(String userId);

    /**
     * 根据id获取钱包信息
     *
     * @param userId 用户id
     * @return UserWallet
     */
    UserWallet getUserWalletById(String userId);


    /**
     * 获取套餐
     *
     * @return List<RechargeCombo>
     */
    List<RechargeCombo> getAllRechargeCombo();

    /**
     * 添加充值套餐
     *
     * @param rechargeComboDTO rechargeComboDTO
     */
    void addRechargeCombo(RechargeComboDTO rechargeComboDTO);


    /**
     * 钱包充值模块
     *
     * @param dto 数据
     * @param userId 用户id
     */
    void toRechargeByUserId(WalletRechargeDTO dto, String userId);


    /**
     * 修改钱包信息
     * @param dto dto
     * @param userId userId
     * @return boolean
     */
    boolean updateWallet(UpdateWalletDTO dto, String userId);

    /**
     * 删除充值套餐
     * @param id id
     * @return 影响条数
     */
    Integer delRechargeCombo(Integer id);
    /**
     * 删除充值套餐（批量）
     * @param ids 集合
     * @return 影响条数
     */
    Integer batchDelRechargeCombo(List<String> ids);

    /**
     * 更新套餐
     * @param id id
     * @param dto 参数
     */
    void updateRechargeCombo(Integer id, UpdateRechargeComboDTO dto);
}
