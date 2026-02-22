package com.jiwu.api.chat.common.vo.ws;

import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群成员变动消息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSMemberChange {
    // 添加
    public static final Integer CHANGE_TYPE_ADD = 1;
    // 移除
    public static final Integer CHANGE_TYPE_REMOVE = 2;
    // 群聊删除
    public static final Integer CHANGE_TYPE_GROUP_DEL =3;

    @Schema(description = "群组id")
    private Long roomId;
    @Schema(description = "变动uid集合")
    private String uid;
    @Schema(description = "变动类型 1加入群组 2移除群组 3删除群组")
    private Integer changeType;
    /**
     * @see ChatActiveStatusEnum
     */
    @Schema(description = "在线状态 1在线 0离线")
    private Integer activeStatus;

    @Schema(description = "最后一次上下线时间")
    private Date lastOptTime;
}
