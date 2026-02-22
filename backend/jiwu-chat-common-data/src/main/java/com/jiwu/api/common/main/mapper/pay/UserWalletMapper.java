package com.jiwu.api.common.main.mapper.pay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.pay.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {
    // 自增钱包 更新
    @Update("UPDATE user_wallet SET balance = balance + #{balance} , points = points + #{points}  WHERE user_id = #{userId}")
    int addWallet(@Param("userId") String userId, @Param("balance") BigDecimal balance, @Param("points") BigDecimal points);

    // 自减钱包 更新
    @Update("UPDATE user_wallet SET balance = balance - #{balance} , points = points - #{points}  WHERE user_id = #{userId}")
    int reduceWallet(@Param("userId") String userId, @Param("balance") BigDecimal balance, @Param("points") BigDecimal points);

}

