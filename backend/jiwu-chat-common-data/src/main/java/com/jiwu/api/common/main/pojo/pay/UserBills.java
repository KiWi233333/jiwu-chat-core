package com.jiwu.api.common.main.pojo.pay;

/**
 * 描述
 *
 * @className: UserBills
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/27 16:18
 */

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("user_bills")
public class UserBills {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;

    @TableField("orders_id")
    private String ordersId;

    /**
     * 代金卷id
     */
    private String voucherId;

    /**
     * 额度
     */
    private BigDecimal amount;

    /**
     * 消费类型名称
     */
    private String title;

    /**
     * 0：支出，1：收入
     */
    private Integer type;

    /**
     * 货币类型 0：余额，1：积分
     */
    private Integer currencyType;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private BigDecimal total;

}
