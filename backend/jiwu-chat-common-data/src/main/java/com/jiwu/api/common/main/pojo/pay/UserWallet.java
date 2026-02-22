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
 * 用户钱包实体类
 *
 * @className: UserWallet
 * @author: Kiwi23333
 * @description: 用户钱包实体类
 * @date: 2023/4/30 15:29
 */
@Data
@TableName("user_wallet")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserWallet  {

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private String userId;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 充值
     */
    private BigDecimal recharge;

    /**
     * 总消费
     */
    private BigDecimal spend;

    /**
     * 积分
     */
    private Long points;


    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
}
