package com.jiwu.api.chat.common.strategy.msg.type;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.MentionInfo;
import com.jiwu.api.common.main.dto.chat.msg.body.TextMsgDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import com.jiwu.api.common.main.dto.chat.vo.TextBodyMsgVO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.common.utils.discover.PrioritizedUrlDiscover;
import com.jiwu.api.common.main.cache.chat.ChatMsgCache;
import com.jiwu.api.common.main.cache.user.UserCache;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description: 普通文本消息
 * Date: 2023-06-04
 */
@Component
public class TextMsgHandler extends AbstractMsgHandler<TextMsgDTO> {

    @Resource
    private ChatMsgCache chatMsgCache;
    @Resource
    private ChatMsgCache msgCache;
    @Resource
    private UserCache userCache;
    @Resource
    private ChatMessageMapper messageMapper;
    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();


    /**
     * 消息类型
     */
    @Override
    protected MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    @Override
    protected void checkMsg(TextMsgDTO body, Long roomId, String uid) {
        // 校验回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            ChatMessage replyMsg = chatMsgCache.getMsg(body.getReplyMsgId());
            AssertUtil.isNotEmpty(replyMsg, "回复消息不存在");
            AssertUtil.equal(replyMsg.getRoomId(), roomId, "只能回复相同会话内的消息");
        }
        // 校验@用户
        if (CollUtil.isNotEmpty(body.getAtUidList())) {
            // 前端传入的@用户列表可能会重复，需要去重
            List<String> atUidList = body.getAtUidList().stream().distinct().collect(Collectors.toList());
            final Map<String, User> userMap = userCache.getUserInfoBatch(atUidList);
            // 如果@用户不存在，userInfoCache 返回的map中依然存在该key，但是value为null，需要过滤掉再校验
            AssertUtil.equal(ResultStatus.NULL_ERR, atUidList.size(), userMap.size(), "@用户不存在！");
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
    protected void saveMsg(ChatMessage msg, TextMsgDTO dto) {
        // 补充校验
        AssertUtil.isTrue(StringUtils.isNotBlank(msg.getContent()), "消息内容不能为空！");
        // 1、附加材料
        ChatMessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new ChatMessageExtra());
        extra.setTextMsgDTO(dto);
        ChatMessage update = new ChatMessage();
        update.setId(msg.getId());
        // 附加
        update.setExtra(extra);
        // 2、如果有回复消息
        if (Objects.nonNull(dto.getReplyMsgId())) {
            LambdaQueryWrapper<ChatMessage> qw = new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getRoomId, msg.getRoomId())
                    .gt(ChatMessage::getId, dto.getReplyMsgId())
                    .le(ChatMessage::getId, msg.getId());
            // 2）统计上一条（与回复的消息间隔多少条）
            update.setGapCount(messageMapper.selectCount(qw));
            // 3）回复的消息ID
            update.setReplyMsgId(dto.getReplyMsgId());
        }
        // 4、判断消息url跳转
        Map<String, UrlInfoDTO> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(msg.getContent());
        extra.setUrlContentMap(urlContentMap);
        // 5、艾特功能
        if (CollUtil.isNotEmpty(dto.getAtUidList())) { // TODO: 旧版待删除
            extra.setAtUidList(dto.getAtUidList());
        }
        if (CollUtil.isNotEmpty(dto.getMentionList())) { // 新版提及 @
            extra.setMentionList(dto.getMentionList());
        }
        // 6、更新
        messageMapper.updateById(update);
    }

    /**
     * 展示消息
     *
     * @param msg 消息
     */
    @Override
    public TextBodyMsgVO showMsg(ChatMessage msg) {
        TextBodyMsgVO resp = new TextBodyMsgVO();
        // 1. 链接消息
        resp.setUrlContentMap(Optional.ofNullable(msg.getExtra()).map(ChatMessageExtra::getUrlContentMap).orElse(null));
        // 2. @用户
        resp.setAtUidList(Optional.ofNullable(msg.getExtra()).map(ChatMessageExtra::getAtUidList).orElse(null)); // TODO: 兼容老版本
        resp.setMentionList(Optional.ofNullable(msg.getExtra()).map(ChatMessageExtra::getMentionList).orElse(null));
        // 3. 回复消息
        Optional<ChatMessage> reply = Optional.ofNullable(msg.getReplyMsgId())
                .map(msgCache::getMsg)
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
            resp.setReply(vo);
        }
        return resp;
    }

    /**
     * 被回复时——展示的消息
     *
     * @param msg 消息
     */
    @Override
    public String showReplyMsg(ChatMessage msg) {
        // 1、获取消息内容
        return msg.getContent();
    }

    /**
     * 会话列表——展示的消息
     *
     * @param msg 消息
     */
    @Override
    public String showContactMsg(ChatMessage msg) {
        return msg.getContent();
    }
}
