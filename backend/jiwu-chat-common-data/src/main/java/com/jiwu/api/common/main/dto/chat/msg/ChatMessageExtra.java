package com.jiwu.api.common.main.dto.chat.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jiwu.api.common.main.dto.chat.msg.body.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Description: 消息扩展属性
 * Date: 2023-05-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageExtra implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    //url跳转链接
    private Map<String, UrlInfoDTO> urlContentMap;
    //消息撤回详情
    private MsgRefundDTO recall;
    //消息删除详情
    private DeleteMsgDTO delete;
    //艾特的uid
    private List<String> atUidList;
    //提及
    private List<MentionInfo> mentionList;
    //文件消息
    private FileMsgDTO fileMsgDTO;
    //图片消息
    private ImgMsgDTO imgMsgDTO;
    //语音消息
    private SoundMsgDTO soundMsgDTO;
    //视频消息
    private VideoMsgDTO videoMsgDTO;
    //文本消息
    private TextMsgDTO textMsgDTO;
    //群通知消息
    private GroupNoticeMsgDTO groupNoticeMsgDTO;

}
