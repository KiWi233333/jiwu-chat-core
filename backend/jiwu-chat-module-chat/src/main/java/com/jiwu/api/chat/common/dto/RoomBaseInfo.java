package com.jiwu.api.chat.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.jiwu.api.common.main.enums.chat.HotFlagEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * Description: 房间详情
 */
@Data
public class RoomBaseInfo {
    @Schema(description = "房间id")
    private Long roomId;
    @Schema(description = "会话名称")
    private String name;
    @Schema(description = "会话头像")
    private String avatar;
    /**
     * 房间类型 1群聊 2单聊
     */
    private Integer type;

    /**
     * 是否全员展示 0否 1是
     *
     * @see HotFlagEnum
     */
    private Integer hotFlag;

    /**
     * 群最后消息的更新时间
     */
    @TableField("active_time")
    private Date activeTime;

    /**
     * 最后一条消息id
     */
    @TableField("last_msg_id")
    private Long lastMsgId;

}
