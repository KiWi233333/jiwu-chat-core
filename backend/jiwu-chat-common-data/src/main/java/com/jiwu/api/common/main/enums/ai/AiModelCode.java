package com.jiwu.api.common.main.enums.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum AiModelCode {
    Kimi(1, "Kimi"),
    XunFei(2, "讯飞星火"),
    DeepSeek(3, "DeepSeek"),
    SiliconFlow(4, "硅基流动"),
    XunFei_DEEPSEEK_R1(5, "讯飞DeepSeek-R1"),
    OpenAi(6, "OpenAI");

    public static final long MIN = 1;
    public static final long MAX = 6;

    public static boolean checkValid(int code) {
        return codeMap.containsKey(code);
    }

    private final int code;
    private final String name;

    private static final Map<Integer, AiModelCode> codeMap = new HashMap<>();

    static {
        for (AiModelCode modelCode : AiModelCode.values()) {
            codeMap.put(modelCode.code, modelCode);
        }
    }

    public static AiModelCode getByCode(Integer modelCode) {
        return codeMap.get(modelCode);
    }

    public static AiModelCode getById(String userId) {
        for (AiModelCode modelCode : AiModelCode.values()) {
            if (modelCode.name.equals(userId)) {
                return modelCode;
            }
        }
        return null;
    }

}
