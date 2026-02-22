package com.jiwu.api.common.main.enums.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiBusinessType {

    // 大模型文本聊天
    TEXT(1, "大模型聊天"),
    // 文生图式聊天
    PHOTO(2, "文生图式聊天"),
    // 文生视频聊天
    VIDEO(3, "文生视频"),
    // 其他业务类型
    ;

    private final int code;
    private final String name;

}
