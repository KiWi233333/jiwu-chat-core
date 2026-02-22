package com.jiwu.api.chat.service.adapter;

import com.jiwu.api.common.main.enums.chat.HotFlagEnum;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.pojo.chat.ChatContact;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.main.pojo.sys.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聊天模块适配器
 */
public class ChatAdapter {
    public static final String SEPARATOR = ",";

    public static String generateRoomKey(List<String> uidList) {
        return uidList.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARATOR));
    }

    public static ChatRoom buildRoom(RoomTypeEnum typeEnum) {
        ChatRoom room = new ChatRoom();
        room.setType(typeEnum.getType());
        room.setHotFlag(HotFlagEnum.NOT.getType());
        return room;
    }

    public static ChatRoomSelf buildFriendRoom(Long roomId, List<String> uidList) {
        List<String> collect = uidList.stream().sorted().collect(Collectors.toList());
        ChatRoomSelf roomFriend = new ChatRoomSelf();
        roomFriend.setRoomId(roomId);
        roomFriend.setUid1(collect.get(0));
        roomFriend.setUid2(collect.get(1));
        roomFriend.setRoomKey(generateRoomKey(uidList));
        roomFriend.setStatus(NormalOrNoEnum.NORMAL.getStatus());
        return roomFriend;
    }

    public static ChatContact buildContact(String uid, Long roomId) {
        ChatContact contact = new ChatContact();
        contact.setRoomId(roomId);
        contact.setUserId(uid);
        return contact;
    }

    public static Set<String> getFriendUidSet(Collection<ChatRoomSelf> values, String uid) {
        return values.stream()
                .map(a -> getFriendUid(a, uid))
                .collect(Collectors.toSet());
    }

    /**
     * 获取好友uid
     */
    public static String getFriendUid(ChatRoomSelf roomFriend, String uid) {
        return Objects.equals(uid, roomFriend.getUid1()) ? roomFriend.getUid2() : roomFriend.getUid1();
    }

    public static ChatRoomGroup buildGroupRoom(User user, Long roomId) {
        ChatRoomGroup roomGroup = new ChatRoomGroup();
        roomGroup.setName(user.getNickname() + "的群组");
        roomGroup.setAvatar(user.getAvatar());
        roomGroup.setRoomId(roomId);
        return roomGroup;
    }

    public static ChatRoomGroup buildGroupRoom(User user, Long roomId,String avatar) {
        ChatRoomGroup roomGroup = new ChatRoomGroup();
        roomGroup.setName(user.getNickname() + "的群组");
        roomGroup.setAvatar(avatar);
        roomGroup.setRoomId(roomId);
        return roomGroup;
    }

    public static ChatRoomGroup buildGroupRoom(User user, Long roomId,String avatar, List<User> users) {
        // 构建名称
        String names = users.stream().map(User::getNickname).collect(Collectors.joining("、"));
        names = names.length() > 30 ? names.substring(0, 30) + "...等人" : names;

        ChatRoomGroup roomGroup = new ChatRoomGroup();
        roomGroup.setName(names  + "的群组");
        roomGroup.setAvatar(avatar);
        roomGroup.setRoomId(roomId);
        return roomGroup;
    }
}
