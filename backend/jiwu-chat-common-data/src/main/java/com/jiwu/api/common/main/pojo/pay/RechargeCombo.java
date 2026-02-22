package com.jiwu.api.common.main.pojo.pay;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值套装实体类
 *
 * @className: RechargeCombo
 * @author: Kiwi23333
 * @description: 充值套装实体类
 * @date: 2023/4/30 21:57
 */
@Data
@TableName("user_recharge_combo")
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
public class RechargeCombo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 套餐描述
     */
    private String name;

    /**
     * 折扣
     */
    private Float discount;

    /**
     * 额度
     */
    private BigDecimal amount;

    /**
     * 送积分
     */
    private Long points;

    @TableField(value="create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;
}
