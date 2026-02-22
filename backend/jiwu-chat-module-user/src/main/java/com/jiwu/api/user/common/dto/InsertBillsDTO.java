package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.pay.UserBills;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 查询帖子dto
 *
 * @className: SelectCommPostDTO
 * @author: Kiwi23333
 * @description:
 * @date: 2023/6/9 2:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class InsertBillsDTO {
    /**
     * 用户id
     */
    String userId;
    /**
     * 收支类型 0：支出，1：收入
     */
    Integer incomeOrOut;
    /**
     * 类型标题名称
     */
    String title;

    /**
     * 订单id
     */
    String orderId;

    /**
     * 货币类型 0：金钱 1：积分
     */
    Integer currencyType;

    /**
     * 额度
     */
    BigDecimal amount;

    /**
     * 代金卷id
     */
    String voucherId;

    public static UserBills toUserBills(InsertBillsDTO dto) {
        return new UserBills()
                .setUserId(dto.userId)
                .setTitle(dto.title)
                .setType(dto.incomeOrOut)
                .setOrdersId(dto.orderId)
                .setCurrencyType(dto.currencyType)
                .setAmount(dto.amount)
                .setVoucherId(dto.voucherId);
    }

}
