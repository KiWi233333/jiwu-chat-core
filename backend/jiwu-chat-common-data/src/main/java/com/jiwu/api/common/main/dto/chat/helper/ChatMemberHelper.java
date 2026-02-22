package com.jiwu.api.common.main.dto.chat.helper;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;

/**
 * 成员列表工具类
 *
 * Description:构建群聊成员信息
 * Date: 2023-12-20
 */
public class ChatMemberHelper {
    private static final String SEPARATOR = ","; // 双id的构建拼接方式

    public static Pair<ChatActiveStatusEnum, String> getCursorPair(String cursor) {
        ChatActiveStatusEnum activeStatusEnum = ChatActiveStatusEnum.ONLINE;
        String timeCursor = null;
        if (StrUtil.isNotBlank(cursor)) {
            String[] t = cursor.split(SEPARATOR);
            String activeStr = t[0];
            String timeStr = t[1];
            activeStatusEnum = ChatActiveStatusEnum.of(Integer.parseInt(activeStr));
            timeCursor = timeStr;
        }
        return Pair.of(activeStatusEnum, timeCursor);
    }

    public static String generateCursor(ChatActiveStatusEnum activeStatusEnum, String timeCursor) {
        return activeStatusEnum.getStatus() + SEPARATOR + timeCursor;
    }
}
