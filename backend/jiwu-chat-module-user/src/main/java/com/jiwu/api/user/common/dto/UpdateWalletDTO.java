package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.pay.UserWallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 钱包充值dto
 *
 * @className: WalletRechargeDTO
 * @author: Kiwi23333
 * @description: 钱包充值dto
 * @date: 2023/4/30 22:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateWalletDTO {

    /**
     * 充值
     */
    private BigDecimal recharge;

    /**
     * 总消费
     */
    private BigDecimal spend;

    public static UserWallet toUserWallet(UpdateWalletDTO dto) {
        return new UserWallet().setRecharge(dto.getRecharge()).setSpend(dto.getSpend());
    }
}
