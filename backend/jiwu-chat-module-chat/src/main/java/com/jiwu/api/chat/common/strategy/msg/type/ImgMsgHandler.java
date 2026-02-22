package com.jiwu.api.chat.common.strategy.msg.type;

import cn.hutool.core.collection.CollUtil;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.ImgMsgDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.MentionInfo;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import com.jiwu.api.common.main.dto.chat.vo.ImgBodyMsgVO;
import com.jiwu.api.common.main.dto.chat.vo.TextBodyMsgVO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.common.utils.discover.PrioritizedUrlDiscover;
import com.jiwu.api.common.main.cache.chat.ChatMsgCache;
import com.jiwu.api.common.main.cache.user.UserCache;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description:图片消息
 * Date: 2023-06-04
 */
@Component
@Slf4j
public class ImgMsgHandler extends AbstractMsgHandler<ImgMsgDTO> {
    @Resource
    private ChatMessageMapper messageMapper;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private UserCache userCache;
    @Resource
    private ChatMsgCache chatMsgCache;
    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();


    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.IMG;
    }

    /**
     * 校验消息
     *
     * @param body   参数
     * @param roomId 房间号
     * @param uid    用户id
     */
    @Override
    protected void checkMsg(ImgMsgDTO body, Long roomId, String uid) {
        super.checkMsg(body, roomId, uid);
        // 校验回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            ChatMessage replyMsg = chatMsgCache.getMsg(body.getReplyMsgId());
            AssertUtil.isNotEmpty(replyMsg, "回复消息不存在");
            AssertUtil.equal(replyMsg.getRoomId(), roomId, "只能回复相同会话内的消息");
        }
        // 校验@用户 新版本
        if (CollUtil.isNotEmpty(body.getMentionList())) {
            // 前端传入的@用户列表可能会重复，需要去重
            List<String> userIds = body.getMentionList().stream().map(MentionInfo::getUid).collect(Collectors.toList());
            final Map<String, User> userMap = userCache.getUserInfoBatch(userIds);
            AssertUtil.equal(ResultStatus.NULL_ERR, userIds.size(), userMap.size(), "@提及用户不存在！");
            // 把前端传递的 mention对比
            for (MentionInfo mentionInfo : body.getMentionList()) {
                final User user = userMap.get(mentionInfo.getUid());
                AssertUtil.isTrue(user != null && user.getNickname().equals(mentionInfo.getDisplayName().replace("@", "")), "@用户不能为空！");
            }
        }
    }

    @Override
    public void saveMsg(ChatMessage msg, ImgMsgDTO dto) {
        String key = dto.getUrl();
        // 1、消费图片文件
        ChatMessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new ChatMessageExtra());
//        // 获取宽高信息 TODO: 暂时前端获取
//        final OssFileUtil.OssImgInfo imgInfo = ossFileUtil.getImgInfo(key);
//        log.info("图片信息：{}", imgInfo);
//        // 2、修改消息
//        if (imgInfo != null) { // 图片信息
//            dto.setWidth(imgInfo.getWidth());
//            dto.setHeight(imgInfo.getHeight());
//            dto.setSize(imgInfo.getSize());
//        }
        // 3、判断消息url跳转
        Map<String, UrlInfoDTO> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(msg.getContent());
        extra.setUrlContentMap(urlContentMap);
        // 5、艾特功能
        if (CollUtil.isNotEmpty(dto.getMentionList())) { // 新版提及 @
            extra.setMentionList(dto.getMentionList());
        }

        extra.setImgMsgDTO(dto);
        messageMapper.updateById(new ChatMessage()
                .setId(msg.getId())
                .setReplyMsgId(dto.getReplyMsgId())
                .setExtra(extra));
        AssertUtil.isFalse(StringUtil.isNullOrEmpty(key) || !ossFileUtil.deleteRedisKey(msg.getFromUid(), key), "图片已失效或不存在！");
    }

    @Override
    public ImgBodyMsgVO showMsg(ChatMessage msg) {
        final ImgMsgDTO imgMsgDTO = Optional.ofNullable(msg.getExtra().getImgMsgDTO()).orElse(new ImgMsgDTO());
        final ImgBodyMsgVO body = ImgBodyMsgVO.builder()
                .url(imgMsgDTO.getUrl())
                .width(imgMsgDTO.getWidth())
                .height(imgMsgDTO.getHeight())
                .build();
        // 1. 链接消息
        body.setUrlContentMap(Optional.ofNullable(msg.getExtra()).map(ChatMessageExtra::getUrlContentMap).orElse(null));
        // 2. @用户
        body.setMentionList(Optional.ofNullable(msg.getExtra()).map(ChatMessageExtra::getMentionList).orElse(null));
        // 3. 回复消息
        Optional<ChatMessage> reply = Optional.ofNullable(msg.getReplyMsgId())
                .map(chatMsgCache::getMsg)
                .filter(a -> Objects.equals(a.getStatus(), MessageStatusEnum.NORMAL.getStatus()));
        // 回复消息
        if (reply.isPresent()) {
            ChatMessage replyMessage = reply.get();
            TextBodyMsgVO.ReplyMsg vo = new TextBodyMsgVO.ReplyMsg();
            vo.setId(replyMessage.getId());
            vo.setUid(replyMessage.getFromUid());
            vo.setType(replyMessage.getType());
            vo.setBody(MsgHandlerFactory.getStrategyNoNull(replyMessage.getType()).showReplyMsg(replyMessage));
            User replyUser = userCache.getUserInfo(replyMessage.getFromUid());
            vo.setNickName(replyUser.getNickname());
            // 消息是否可跳转
            // vo.setCanCallback(YesOrNoEnum.toStatus(Objects.nonNull(msg.getGapCount()) && msg.getGapCount() <= MessageAdapter.CAN_CALLBACK_GAP_COUNT));
            vo.setGapCount(msg.getGapCount());
            body.setReply(vo);
        }
        return body;
    }

    @Override
    public Object showReplyMsg(ChatMessage msg) {
        return String.format("[图片] %s", !StringUtil.isNullOrEmpty(msg.getContent()) ? " " + msg.getContent() : "");
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return String.format("[图片] %s", !StringUtil.isNullOrEmpty(msg.getContent()) ? msg.getContent() : "");
    }
}
