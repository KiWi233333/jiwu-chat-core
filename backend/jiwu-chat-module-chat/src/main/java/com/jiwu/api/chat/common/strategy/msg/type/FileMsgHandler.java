package com.jiwu.api.chat.common.strategy.msg.type;

import cn.hutool.core.collection.CollUtil;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.FileMsgDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.MentionInfo;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import com.jiwu.api.common.main.dto.chat.vo.FileBodyMsgVO;
import com.jiwu.api.common.main.dto.chat.vo.TextBodyMsgVO;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.common.utils.discover.PrioritizedUrlDiscover;
import com.jiwu.api.common.main.cache.chat.ChatMsgCache;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.FileMimeTypeEnum;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.model.FileInfo;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description:文件消息
 * Date: 2023-06-04
 */
@Slf4j
@Component
public class FileMsgHandler extends AbstractMsgHandler<FileMsgDTO> {
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
        return MessageTypeEnum.FILE;
    }

    @Override
    protected void checkMsg(FileMsgDTO body, Long roomId, String uid) {
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
    public void saveMsg(ChatMessage msg, FileMsgDTO dto) {
        ChatMessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new ChatMessageExtra());

        // 1、获取文件类型
        try {
            FileInfo info = ossFileUtil.getBucketManager().stat(ossFileUtil.getBucketName(), dto.getUrl());
            dto.setFileName(dto.getFileName());
            dto.setFileType(FileMimeTypeEnum.checkType(info.mimeType));
            dto.setMimeType(info.mimeType); // 文件类型 mimeType
            dto.setSize(info.fsize); // 上传大小字节
        } catch (QiniuException e) {
            throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "上传文件类型错误！");
        }
        // 3、判断消息url跳转
        Map<String, UrlInfoDTO> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(msg.getContent());
        extra.setUrlContentMap(urlContentMap);
        // 5、艾特功能
        if (CollUtil.isNotEmpty(dto.getMentionList())) { // 新版提及 @
            extra.setMentionList(dto.getMentionList());
        }
        extra.setFileMsgDTO(dto);
        // 3、修改消息
        messageMapper.updateById(new ChatMessage()
                .setId(msg.getId())
                .setReplyMsgId(dto.getReplyMsgId())
                .setExtra(extra)
        );
        // 4、消费文件文件
        String key = extra.getFileMsgDTO().getUrl();
        if (StringUtil.isNullOrEmpty(key) || !ossFileUtil.deleteRedisKey(msg.getFromUid(), key)) {
            throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "文件已失效或不存在！");
        }
    }

    @Override
    public Object showMsg(ChatMessage msg) {
        final FileMsgDTO fileMsgDTO = Optional.ofNullable(msg.getExtra().getFileMsgDTO()).orElse(new FileMsgDTO());
        final FileBodyMsgVO body = FileBodyMsgVO.builder()
                .fileName(fileMsgDTO.getFileName())
                .mimeType(fileMsgDTO.getMimeType())
                .size(fileMsgDTO.getSize())
                .fileType(fileMsgDTO.getFileType())
                .url(fileMsgDTO.getUrl())
                .mentionList(fileMsgDTO.getMentionList())
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
        return showContactMsg(msg);
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return String.format("[文件] %s %s",
                Optional.ofNullable(msg.getExtra())
                        .map(ChatMessageExtra::getFileMsgDTO)
                        .map(FileMsgDTO::getFileName)
                        .orElse(""),
                Optional.ofNullable(msg.getContent()).filter(content -> !StringUtil.isNullOrEmpty(content)).orElse(""));
    }
}
