package com.jiwu.api.common.main.pojo.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jiwu.api.common.main.secure_invoke.dto.SysSecureInvokeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 系统操作快照@
 * Description:
 * Date: 2023-08-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_secure_invoke_record", autoResultMap = true)
public class SysSecureInvokeRecord {

    public static final byte STATUS_WAIT = 1;
    public static final byte STATUS_FAIL = 2;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 请求快照参数json
     */
    @TableField(value = "secure_invoke_json", typeHandler = JacksonTypeHandler.class)
    private SysSecureInvokeDTO sysSecureInvokeDTO;
    /**
     * 状态 1待执行 2已失败
     */
    @TableField("status")
    @Builder.Default
    private byte status = SysSecureInvokeRecord.STATUS_WAIT;
    /**
     * 下一次重试的时间
     */
    @TableField("next_retry_time")
    @Builder.Default
    private Date nextRetryTime = new Date();
    /**
     * 已经重试的次数
     */
    @TableField("retry_times")
    @Builder.Default
    private Integer retryTimes = 0;
    @TableField("max_retry_times")
    private Integer maxRetryTimes;
    @TableField("fail_reason")
    private String failReason;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;

}
