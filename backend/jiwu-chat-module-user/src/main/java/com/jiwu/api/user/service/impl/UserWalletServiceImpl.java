package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.main.mapper.pay.RechargeComboMapper;
import com.jiwu.api.common.main.mapper.pay.UserWalletMapper;
import com.jiwu.api.common.main.pojo.pay.RechargeCombo;
import com.jiwu.api.common.main.pojo.pay.UserWallet;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.user.common.dto.RechargeComboDTO;
import com.jiwu.api.user.common.dto.UpdateRechargeComboDTO;
import com.jiwu.api.user.common.dto.WalletRechargeDTO;
import com.jiwu.api.user.common.dto.UpdateWalletDTO;
import com.jiwu.api.user.common.enums.BillsTitleType;
import com.jiwu.api.common.main.enums.user.CurrencyType;
import com.jiwu.api.user.service.UserBillsService;
import com.jiwu.api.user.service.UserWalletService;
import com.jiwu.api.common.constant.UserConstant;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包业务层
 *
 * @className: UserWalletService
 * @author: Kiwi23333
 * @description: 钱包的增删查改
 * @date: 2023/4/30 15:49
 */
@Service
@Slf4j
public class UserWalletServiceImpl implements UserWalletService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserWalletMapper userWalletMapper;

    /**
     * 初始化钱包（插入一条用户数据）
     *
     * @param userId 用户id
     * @return 返回受影响的函数
     */
    @Override
    public int initUserWallet(String userId) {
        int flag = 0;
        try {
            if (StringUtil.isNullOrEmpty(userId)) {
                return 0;
            }
            BigDecimal inits = new BigDecimal("0.00");
            UserWallet userWallet = new UserWallet()
                    .setUserId(userId)
                    .setBalance(inits)
                    .setPoints(500L)
                    .setRecharge(inits)
                    .setSpend(inits);
            flag = userWalletMapper.insert(userWallet);
            if (flag > 0) {
                redisUtil.set(UserConstant.USER_WALLET_KEY + userId, userWallet);
            }
            return flag;
        } catch (Exception e) {
            log.error("插入钱包数据错误error {}", e.getMessage());
            return flag;
        }
    }

    /**
     * 根据id获取钱包信息
     *
     * @param userId 用户id
     * @return UserWallet
     */
    @Override
    public UserWallet getUserWalletById(String userId) {
        // 1、获取
        UserWallet userWallet = getWalletById(userId);
        // 2、判断
        if (userWallet != null) {
            return userWallet;
        } else {
            throw new BusinessException(ResultStatus.SELECT_ERR.getCode(), "获取失败！");
        }
    }

    /**
     * 充值套餐类
     */
    @Autowired
    RechargeComboMapper rechargeComboMapper;

    /**
     * 获取套餐
     *
     * @return List<RechargeCombo>
     */
    @Override
    public List<RechargeCombo> getAllRechargeCombo() {
        Object data = redisUtil.get(UserConstant.USER_RECHARGE_COMBO_KEY);
        if (data != null) {
            return (List<RechargeCombo>) data;
        }
        List<RechargeCombo> list = rechargeComboMapper.selectList(null);
        if (!list.isEmpty()) {
            redisUtil.set(UserConstant.USER_RECHARGE_COMBO_KEY, list);
        }
        return list;
    }


    /**
     * 添加充值套餐
     *
     * @param rechargeComboDTO rechargeComboDTO
     */
    @Override
    public void addRechargeCombo(RechargeComboDTO rechargeComboDTO) {
        if (rechargeComboMapper.insert(RechargeComboDTO.toRechargeCombo(rechargeComboDTO)) <= 0) {
            throw new BusinessException("添加失败！");
        }
        redisUtil.delete(UserConstant.USER_RECHARGE_COMBO_KEY);
    }


    /**
     * 删除套餐（单个）
     *
     * @param id 套餐id
     * @return 影响条数
     */
    @Override
    public Integer delRechargeCombo(Integer id) {
        if (rechargeComboMapper.deleteById(id) == 0) {
            throw new BusinessException(ResultStatus.DELETE_ERR.getCode(), "删除失败，套餐不存在！");
        }
        redisUtil.delete(UserConstant.USER_RECHARGE_COMBO_KEY);
        return 1;
    }

    /**
     * 删除套餐（批量）
     *
     * @param ids ids集合
     * @return 影响条数
     */
    @Override
    @Transactional
    public Integer batchDelRechargeCombo(List<String> ids) {
        // 1、部分删除
        if (rechargeComboMapper.deleteBatchIds(ids) != ids.size()) {
            throw new BusinessException(ResultStatus.DELETE_ERR.getCode(), "删除失败，套餐不存在！");
        }
        // 2、删除缓存
        redisUtil.delete(UserConstant.USER_RECHARGE_COMBO_KEY);
        return ids.size();
    }

    @Override
    public void updateRechargeCombo(Integer id, UpdateRechargeComboDTO dto) {
        // 1、修改
        if (rechargeComboMapper.updateById(UpdateRechargeComboDTO.toRechargeCombo(dto).setId(id)) != 1) {
            throw new BusinessException(ResultStatus.UPDATE_ERR.getCode(), "修改失败，请稍后再试！");
        }
        // 2、删除缓存
        redisUtil.delete(UserConstant.USER_RECHARGE_COMBO_KEY);
    }

    @Autowired
    UserBillsService billsService;

    /**
     * 钱包充值模块
     *
     * @param dto    参数
     * @param userId 用户id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void toRechargeByUserId(WalletRechargeDTO dto, String userId) {
        // 1、获取原有钱包
        UserWallet oldWallet = getWalletById(userId);
        if (oldWallet == null) {
            throw new BusinessException("充值失败，请稍后再试看！");
        }
        // 2、新钱包
        UserWallet newWallet = new UserWallet().setUserId(userId);
        // 3、判断充值模式
        boolean isSaveBills = false;
        if (dto.getType() == 0) {// 1）任意充值
            // 追加余额 追加充值额
            newWallet.setRecharge(oldWallet.getRecharge().add(dto.getAmount()))// +总充值额
                    .setBalance(oldWallet.getBalance().add(dto.getAmount()));// +余额
            userWalletMapper.updateById(newWallet);
            // 保存账单 钱包充值
            isSaveBills = billsService.saveRechargeBills(userId, BillsTitleType.IN_RECHARGE.getVal(), CurrencyType.BALANCE.getKey(), dto.getAmount());
        } else {// 2）套装id充值
            // 套餐充值
            RechargeCombo rechargeCombo = rechargeComboMapper.selectById(dto.getId());
            BigDecimal comboAmount = rechargeCombo.getAmount();
            Long comboPoints = rechargeCombo.getPoints();
            // 充值充值
            newWallet.setRecharge(oldWallet.getRecharge().add(comboAmount))// +总充值额
                    .setBalance(oldWallet.getBalance().add(comboAmount))// +余额
                    .setPoints(oldWallet.getPoints() + comboPoints);// 送积分
            userWalletMapper.updateById(newWallet);
            // 保存账单
            isSaveBills = billsService.saveRechargeBills(userId, BillsTitleType.IN_RECHARGE.getVal(), CurrencyType.BALANCE.getKey(), comboAmount);
            isSaveBills = billsService.saveRechargeBills(userId, BillsTitleType.IN_RECHARGE_POINT.getVal(), CurrencyType.POINT.getKey(), BigDecimal.valueOf(comboPoints));
        }
        if (!isSaveBills) {
            throw new BusinessException("充值失败，请稍后再试看！");
        }
        redisUtil.delete(UserConstant.USER_WALLET_KEY + userId);
    }


    // 1）获取用户钱包
    private UserWallet getWalletById(String id) {
        UserWallet userWallet = (UserWallet) redisUtil.get(UserConstant.USER_WALLET_KEY + id);
        if (userWallet == null) {
            userWallet = userWalletMapper.selectById(id);// 数据库获取
        }
        if (userWallet != null) {
            redisUtil.set(UserConstant.USER_WALLET_KEY + id, userWallet);
            return userWallet;
        } else {
            return null;
        }
    }

    @Autowired
    UserBillsService userBillsService;

    @Override
    public boolean updateWallet(UpdateWalletDTO dto, String userId) {
        LambdaQueryWrapper<UserWallet> qw = new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId);
        // 修改用户钱包
        int flag = userWalletMapper.update(UpdateWalletDTO.toUserWallet(dto), qw);
        if (flag == 1) {
            // 删除缓存
            redisUtil.delete(UserConstant.USER_WALLET_KEY + userId);
            return true;
        } else {
            return false;
        }
    }


}
