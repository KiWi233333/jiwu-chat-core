package com.jiwu.api.chat.common.strategy.msg.type;

import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.GroupNoticeMsgDTO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatGroupMemberService;
import com.jiwu.api.common.main.cache.chat.ChatMsgCache;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.dto.chat.vo.NoticeBodyMsgVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 群通知消息处理器
 */
@Slf4j
@Component
public class GroupNoticeMsgHandler extends AbstractMsgHandler<GroupNoticeMsgDTO> {

    @Resource
    private ChatMessageMapper messageMapper;
    @Resource
    private ChatMsgCache chatMsgCache;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private ChatGroupMemberService chatGroupMemberService;
    @Resource
    private UserCache userCache;

    @Override
    protected MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.GROUP_NOTICE;
    }

    @Override
    protected void checkMsg(GroupNoticeMsgDTO dto, Long roomId, String uid) {
        // 群通知校验权限 (群主)
        List<ChatGroupMember> member = chatGroupMemberService.hasPermission(GroupRoleEnum.HOME, roomId, uid);
        AssertUtil.isNotEmpty(member, "权限不足，群通知仅限群主！");
        // 校验回复消息是否存在
        if (Objects.nonNull(dto.getReplyMsgId())) {
            ChatMessage replyMsg = chatMsgCache.getMsg(dto.getReplyMsgId());
            AssertUtil.isNotEmpty(replyMsg, "回复消息不存在");
            AssertUtil.equal(replyMsg.getRoomId(), roomId, "只能回复相同会话内的消息");
        }
    }

    @Override
    public void saveMsg(ChatMessage msg, GroupNoticeMsgDTO dto) {
        AssertUtil.isNotEmpty(msg.getContent(), "内容不能为空！");
//        AssertUtil.isTrue(msg.getContent().length() <= MsgContentConstant.MAX_TEXT_NOTIFY_LENGTH, String.format("内容不超过%d字符！", MsgContentConstant.MAX_TEXT_NOTIFY_LENGTH));
        // 消费图片
        if (Objects.nonNull(dto.getImgList()) && !dto.getImgList().isEmpty()) {
            final long l = ossFileUtil.deleteRedisKey(msg.getFromUid(), dto.getImgList());
            AssertUtil.isTrue(l != dto.getImgList().size(), "部分图片消费失败！");
        }
        // 更新消息
        ChatMessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new ChatMessageExtra());
        ChatMessage update = new ChatMessage()
                .setId(msg.getId())
                .setReplyMsgId(dto.getReplyMsgId());
        extra.setGroupNoticeMsgDTO(dto);
        update.setExtra(extra);
        log.info("更新消息,{}", update);
        messageMapper.updateById(update);
    }

    @Override
    public NoticeBodyMsgVO showMsg(ChatMessage msg) {
        NoticeBodyMsgVO resp = new NoticeBodyMsgVO();
        // 回复消息
        Optional<ChatMessage> reply = Optional.ofNullable(msg.getReplyMsgId())
                .map(chatMsgCache::getMsg)
                .filter(a -> Objects.equals(a.getStatus(), MessageStatusEnum.NORMAL.getStatus()));
        // 回复消息
        if (reply.isPresent()) {
            ChatMessage replyMessage = reply.get();
            NoticeBodyMsgVO.ReplyMsg vo = new NoticeBodyMsgVO.ReplyMsg();
            vo.setId(replyMessage.getId());
            vo.setUid(replyMessage.getFromUid());
            vo.setType(replyMessage.getType());
            vo.setBody(MsgHandlerFactory.getStrategyNoNull(replyMessage.getType()).showReplyMsg(replyMessage));
            User replyUser = userCache.getUserInfo(replyMessage.getFromUid());
            vo.setNickName(replyUser.getNickname());
            vo.setGapCount(msg.getGapCount());
            resp.setReply(vo);
        }
        if (Objects.nonNull(msg.getExtra()) && Objects.nonNull(msg.getExtra().getGroupNoticeMsgDTO())) {
            // 图片
            GroupNoticeMsgDTO groupNoticeMsgDTO = msg.getExtra().getGroupNoticeMsgDTO();
            if (Objects.nonNull(groupNoticeMsgDTO.getImgList()) && !groupNoticeMsgDTO.getImgList().isEmpty()) {
                resp.setImgList(groupNoticeMsgDTO.getImgList());
            }
            resp.setNoticeAll(groupNoticeMsgDTO.getNoticeAll()); // 通知全员设置
        }
        return resp;
    }

    @Override
    public String showReplyMsg(ChatMessage msg) {
        return String.format("[群通知]: %s", msg.getContent());
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return msg.getContent();
    }
}
