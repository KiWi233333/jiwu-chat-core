package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息表情反应类型枚举
 *
 * @author Kiwi23333
 * @description 前端根据 code 映射自定义图标
 * @date 2026/02/17
 */
@AllArgsConstructor
@Getter
public enum EmojiTypeEnum {

    // ---- 第一梯队：高频基础表情 ----
    THUMBS_UP("thumbs_up", "👍"),
    HEART("heart", "❤️"),
    LAUGH("laugh", "😂"),
    FIRE("fire", "🔥"),
    CLAP("clap", "👏"),
    PRAY("pray", "🙏"),
    // ---- 第二梯队：常用情绪与社交 ----
    PARTY("party", "🎉"),
    THUMBS_DOWN("thumbs_down", "👎"),
    CRY_LAUGH("cry_laugh", "🤣"),
    LOVE_EYES("love_eyes", "😍"),
    SURPRISED("surprised", "😮"),
    SAD("sad", "😢"),
    // ---- 第三梯队：态度与反馈 ----
    ANGRY("angry", "😡"),
    THINK("think", "🤔"),
    EYES("eyes", "👀"),
    HUNDRED("hundred", "💯"),
    ROCKET("rocket", "🚀"),
    OK_HAND("ok_hand", "👌"),
    // ---- 第四梯队：补充表情 ----
    SPARKLES("sparkles", "✨"),
    COOL("cool", "😎"),
    HUG("hug", "🤗"),
    MUSCLE("muscle", "💪"),
    CHECK("check", "✅"),
    WAVE("wave", "👋"),
    ;

    /**
     * 编码（存入数据库）
     */
    private final String code;

    /**
     * Unicode 表情（仅参考，前端使用自定义图标）
     */
    private final String unicode;

    private static final Map<String, EmojiTypeEnum> cache;

    static {
        cache = Arrays.stream(EmojiTypeEnum.values())
                .collect(Collectors.toMap(EmojiTypeEnum::getCode, Function.identity()));
    }

    public static EmojiTypeEnum of(String code) {
        return cache.get(code);
    }

    /**
     * 校验编码是否合法
     */
    public static boolean isValid(String code) {
        return cache.containsKey(code);
    }
}
