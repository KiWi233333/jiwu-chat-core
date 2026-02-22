package com.jiwu.api.chat.common.strategy.msg;


import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class MsgHandlerFactory {
    private static final Map<Integer, AbstractMsgHandler> MSG_MAP_CALLBACK = new HashMap<>();

    public static void register(MessageTypeEnum type, AbstractMsgHandler strategy) {
        MSG_MAP_CALLBACK.put(type.getType(), strategy);
    }

    public static AbstractMsgHandler getStrategyNoNull(Integer code) {
        return MSG_MAP_CALLBACK.get(code);
    }
}
