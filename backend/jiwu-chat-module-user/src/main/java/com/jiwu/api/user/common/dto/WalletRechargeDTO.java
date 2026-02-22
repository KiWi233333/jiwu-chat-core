package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class WalletRechargeDTO {
    @Schema(description = "套餐id", example = "type=1")
    String id;

    @Schema(description = "充值类型", example = "0 任意金额，1 套餐")
    @NotNull(message = "充值类型不能为空！")
    @Range(min = 0, max = 1, message = "充值类型错误！")
    Integer type;

    @Schema(description = "充值金额", example = "type=0")
    @DecimalMin(value = "0.01", message = "充值金额不能小于0.01元！")
    @DecimalMax(value = "10000", message = "充值金额不能大于10000元！")
    BigDecimal amount;


}
