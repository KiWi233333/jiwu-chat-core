package com.jiwu.api.common.main.dto.chat.cursor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.GroupNoticeMsgDTO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.common.util.common.AssertUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 消息内容常量类
 */
public class MsgContentConstant {
    public static final int MAX_TEXT_CONTENT_LENGTH = 2048;

    /**
     * @see GroupNoticeMsgDTO
     */
    public static final int MAX_TEXT_NOTIFY_LENGTH = 4096;
    /**
     * 系统消息内容最大长度
     * 目前用于系统通知、群公告等
     */
    public static final int MAX_TEXT_SYSTEM_LENGTH = 1024 * 10;
    /**
     * AI消息内容最大长度
     * 目前用于AI聊天等
     */
    public static final int MAX_TEXT_AI_LENGTH = 1024 * 8;
    public static final int MAX_TEXT_CALL_LENGTH = 512;


    @JsonIgnore
    private static final Map<Integer, MsgConstantInfo> msgConstantInfoMap = getMsgConstantInfoMap();

    @JsonIgnore
    public static void checkContentLen(ChatMessageDTO dto) {
        MsgConstantInfo constantInfo = msgConstantInfoMap.get(dto.getMsgType());

        if (constantInfo == null) {
            return;
        }
        int maxLen = constantInfo.getMaxLength();
        String msg = String.format("消息内容不能超过%d个字符！", maxLen);
        // 不检查
        if (Objects.equals(dto.getMsgType(), MessageTypeEnum.SOUND.getType())) {
            AssertUtil.isTrue(StringUtils.isBlank(dto.getContent()), "语音消息不允许携带文本内容！");
            return;
        }
        // 图片、文件、视频等类型的消息内容长度检查 可选
        if (Objects.equals(dto.getMsgType(), MessageTypeEnum.IMG.getType())
                || Objects.equals(dto.getMsgType(), MessageTypeEnum.FILE.getType())
                || Objects.equals(dto.getMsgType(), MessageTypeEnum.VIDEO.getType())) {
            AssertUtil.checkSecondWhenFirstTrue(StringUtils.isNotBlank(dto.getContent()), dto.getContent() != null && dto.getContent().length() <= maxLen, msg);
            return;
        }

        // 文本消息类型的内容长度检查
        AssertUtil.isTrue(StringUtils.isNotBlank(dto.getContent()) && dto.getContent().length() <= maxLen, msg);
    }

    /**
     * 消息常量信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MsgConstantInfo {

        @Schema(description = "消息文本最大长度")
        private int maxLength;
    }

    // 获取消息类型与常量信息的映射
    public static Map<Integer, MsgConstantInfo> getMsgConstantInfoMap() {
        Map<Integer, MsgConstantInfo> map = new HashMap<>();
        map.put(MessageTypeEnum.AI_CHAT.getType(), new MsgConstantInfo(MAX_TEXT_AI_LENGTH));
        map.put(MessageTypeEnum.SYSTEM.getType(), new MsgConstantInfo(MAX_TEXT_SYSTEM_LENGTH));
        map.put(MessageTypeEnum.GROUP_NOTICE.getType(), new MsgConstantInfo(MAX_TEXT_NOTIFY_LENGTH));
        map.put(MessageTypeEnum.TEXT.getType(), new MsgConstantInfo(MAX_TEXT_CONTENT_LENGTH));
        map.put(MessageTypeEnum.IMG.getType(), new MsgConstantInfo(MAX_TEXT_CONTENT_LENGTH));
        map.put(MessageTypeEnum.FILE.getType(), new MsgConstantInfo(MAX_TEXT_CONTENT_LENGTH));
        map.put(MessageTypeEnum.VIDEO.getType(), new MsgConstantInfo(MAX_TEXT_CONTENT_LENGTH));
        return map;
    }
}
