package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子活动参数
 *
 * @className: UserStatus
 * @author: Kiwi23333
 * @description: 帖子活动参数
 * @date: 2023/4/11 16:12
 */
@Getter
@AllArgsConstructor
public enum PostActionStatus {

    COLLECT("收藏", 0),// 0:待付款
    LIKE("点赞", 1),// 1:已付款
    ;
    private final String key;
    private final Integer val;




}
