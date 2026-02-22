package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友申请状态枚举（原 RtcSignalTypeEnum.ApplyStatusEnum，开源版仅保留基础聊天）
 */
@Getter
@AllArgsConstructor
public enum FriendApplyStatusEnum {

    WAIT_APPROVAL(0, "待审批"),
    AGREE(1, "同意"),
    REJECT(2, "拒绝");

    private final Integer code;
    private final String desc;
}
