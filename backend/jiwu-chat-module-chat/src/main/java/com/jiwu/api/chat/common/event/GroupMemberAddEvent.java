package com.jiwu.api.chat.common.event;

import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 添加群成员事件
 */
@Getter
public class GroupMemberAddEvent extends ApplicationEvent {

    private final List<ChatGroupMember> memberList;
    private final ChatRoomGroup roomGroup;
    private final String inviteUid; // 发起人id

    /**
     * 添加群成员事件
     */
    public GroupMemberAddEvent(Object source, ChatRoomGroup roomGroup, List<ChatGroupMember> memberList, String inviteUid) {
        super(source);
        this.memberList = memberList;
        this.roomGroup = roomGroup;
        this.inviteUid = inviteUid;
    }

}
