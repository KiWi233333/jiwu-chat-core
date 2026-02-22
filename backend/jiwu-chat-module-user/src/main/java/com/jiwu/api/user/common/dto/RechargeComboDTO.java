package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.pay.RechargeCombo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;


/**
 * 充值套餐参数类
 *
 * @className: RechargeComboDTO
 * @author: Kiwi23333
 * @description: RechargeComboDTO
 * @date: 2023/5/5 14:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RechargeComboDTO {

    @Schema(description = "套餐名称")
    @NotBlank(message = "套餐名称不能为空")
    @Length(min = 1, max = 30, message = "套餐名称超出长度（1-30）！")
    private String name;

    @Schema(description = "折扣", defaultValue = "1.0")
    @DecimalMin(value = "0.01", message = "折扣不能低于0.01！")
    @DecimalMax(value = "1.00", message = "折扣不能高于1.00！")
    @Digits(integer = 1, fraction = 2, message = "折扣范围为0.01-1.00")
    private BigDecimal discount;

    @Schema(description = "套餐额度", requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "1.00", message = "套餐额度不能低于1元！")
    @DecimalMax(value = "10000.00", message = "套餐额度不能高于10000元！")
    private BigDecimal amount;

    @Schema(description = "送积分额度", requiredMode = Schema.RequiredMode.REQUIRED)
    @Max(value = 10000, message = "积分额度不能高于10000积分！")
    private Long points;

    public static RechargeCombo toRechargeCombo(RechargeComboDTO re) {
        return new RechargeCombo()
                .setPoints(re.getPoints())
                .setName(re.getName())
                .setAmount(re.getAmount())
                .setDiscount(re.getDiscount() == null ? 1.00f : re.getDiscount().floatValue());
    }
}
