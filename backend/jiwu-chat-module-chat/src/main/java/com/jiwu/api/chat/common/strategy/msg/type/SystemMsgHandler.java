package com.jiwu.api.chat.common.strategy.msg.type;

import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description:系统消息
 * Date: 2023-06-04
 */
@Component
public class SystemMsgHandler extends AbstractMsgHandler<String> {

    @Autowired
    private ChatMessageMapper messageMapper;
    @Autowired
    private ChatGroupMemberService chatGroupMemberService;

    @Override
    protected MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SYSTEM;
    }

    @Override
    public void saveMsg(ChatMessage msg, String body) {
        ChatMessage update = new ChatMessage();
        update.setId(msg.getId());
        AssertUtil.isNotEmpty(msg.getContent(), "内容不能为空！");
//        AssertUtil.isTrue(msg.getContent().length() <= MsgContentConstant.MAX_TEXT_SYSTEM_LENGTH, String.format("内容不超过%d字符！", MsgContentConstant.MAX_TEXT_SYSTEM_LENGTH));
        update.setContent(msg.getContent()); // 内容在content
        messageMapper.updateById(update);
    }

    @Override
    protected void checkMsg(String body, Long roomId, String uid) {
        // 系统消息校验权限 (群主)
        List<ChatGroupMember> member = chatGroupMemberService.hasPermission(GroupRoleEnum.HOME, roomId, uid);
        AssertUtil.isNotEmpty(member, "权限不足！");
    }

    @Override
    public String showMsg(ChatMessage msg) {
        return msg.getContent();
    }

    @Override
    public String showReplyMsg(ChatMessage msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return msg.getContent();
    }
}
