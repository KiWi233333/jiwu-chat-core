package com.jiwu.api.chat.common.strategy.msg.type;

import cn.hutool.core.collection.CollUtil;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.main.dto.chat.vo.TextBodyMsgVO;
import com.jiwu.api.common.main.dto.chat.vo.VideoMsgVO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.common.utils.discover.PrioritizedUrlDiscover;
import com.jiwu.api.common.main.cache.chat.ChatMsgCache;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.MentionInfo;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.VideoMsgDTO;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Description:视频消息
 * Date: 2023-06-04
 */
@Component
public class VideoMsgHandler extends AbstractMsgHandler<VideoMsgDTO> {
    @Resource
    private ChatMessageMapper messageMapper;
    @Resource
    OssFileUtil ossFileUtil;
    @Resource
    private UserCache userCache;
    @Resource
    private ChatMsgCache chatMsgCache;

    // 视频封面采取前端获取，封面（1m..以下）和视频同步上传，合并后发送消息并校验，后端md5简单比对（待定）

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.VIDEO;
    }

    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();

    /**
     * 校验消息
     *
     * @param body   参数
     * @param roomId 房间号
     * @param uid    用户id
     */
    @Override
    protected void checkMsg(VideoMsgDTO body, Long roomId, String uid) {
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

    /**
     * 保存消息
     *
     * @param msg 消息
     * @param dto 参数
     */
    @Override
    public void saveMsg(ChatMessage msg, VideoMsgDTO dto) {
        ChatMessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new ChatMessageExtra());
        // 1、消费个人上传封面图片
        String coverKey = dto.getThumbUrl();
        AssertUtil.isFalse(StringUtil.isNullOrEmpty(coverKey), ResultStatus.NULL_ERR, "封面图片不能为空！");
        AssertUtil.isFalse(StringUtil.isNullOrEmpty(coverKey) || !ossFileUtil.deleteRedisKey(msg.getFromUid(), coverKey), ResultStatus.NULL_ERR, "封面图片已经失效，请重新上传视频！");
        // 2、消费视频文件
        String videoKey = dto.getUrl();
        if (StringUtil.isNullOrEmpty(videoKey) || !ossFileUtil.deleteRedisKey(msg.getFromUid(), videoKey)) {
            ossFileUtil.deleteRedisKey(msg.getFromUid(), coverKey);// 封面删除
            throw new BusinessException(ResultStatus.NULL_ERR, "视频文件已经失效，请重新上传视频！");
        }
        // 3、判断消息url跳转
        Map<String, UrlInfoDTO> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(msg.getContent());
        extra.setUrlContentMap(urlContentMap);
        // 4、艾特功能
        if (CollUtil.isNotEmpty(dto.getMentionList())) { // 新版提及 @
            extra.setMentionList(dto.getMentionList());
        }
        // 5、更新消息扩展
        extra.setVideoMsgDTO(dto);
        messageMapper.updateById(new ChatMessage()
                .setId(msg.getId())
                .setReplyMsgId(dto.getReplyMsgId())
                .setExtra(extra)
        );
    }

    @Override
    public Object showMsg(ChatMessage msg) {
        final VideoMsgDTO videoMsgDTO = Optional.ofNullable(msg.getExtra().getVideoMsgDTO()).orElse(new VideoMsgDTO());
        final VideoMsgVO body = VideoMsgVO.builder()
                .url(videoMsgDTO.getUrl())
                .size(videoMsgDTO.getSize())
                .duration(videoMsgDTO.getDuration())
                .thumbUrl(videoMsgDTO.getThumbUrl())
                .thumbWidth(videoMsgDTO.getThumbWidth())
                .thumbHeight(videoMsgDTO.getThumbHeight())
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
    public String showReplyMsg(ChatMessage msg) {
        return String.format("[视频] %s", !StringUtil.isNullOrEmpty(msg.getContent()) ? msg.getContent() : "");
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return String.format("[视频] %s", !StringUtil.isNullOrEmpty(msg.getContent()) ? msg.getContent() : "");
    }
}
