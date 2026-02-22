package com.jiwu.api.common.main.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.mapper.chat.ChatRoomSelfMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单聊房间DAO
 *
 * @className: ChatRoomSelfDAO
 * @author: Kiwi23333
 * @description:
 * @date: 2023/12/26 11:47
 */
@Service
public class ChatRoomSelfDAO extends ServiceImpl<ChatRoomSelfMapper, ChatRoomSelf> {

    public ChatRoomSelf getByKey(String key) {
        return lambdaQuery().eq(ChatRoomSelf::getRoomKey, key).one();
    }

    /**
     * 分页获取列表
     *
     * @param dto    参数
     * @param userId 用户id
     * @param status 状态
     * @return 参数
     */
    public CursorPageBaseVO<ChatRoomSelf> getCursorPage(CursorPageBaseDTO dto, String userId, MessageStatusEnum status) {
        return CursorUtils.getCursorPageByMysql(this, dto, wrapper -> {
            wrapper.eq(ChatRoomSelf::getUid1, userId).or(p -> p.eq(ChatRoomSelf::getUid2, userId));
            wrapper.eq(ChatRoomSelf::getStatus, status.getStatus());
        }, ChatRoomSelf::getCreateTime, Date.class);
    }


    public boolean disableRoom(String key) {
        return lambdaUpdate()
                .eq(ChatRoomSelf::getRoomKey, key)
                .set(ChatRoomSelf::getStatus, NormalOrNoEnum.NOT_NORMAL.getStatus())
                .update();
    }

    public void restoreRoom(Long id) {
        lambdaUpdate()
                .eq(ChatRoomSelf::getId, id)
                .set(ChatRoomSelf::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .update();
    }

    public ChatRoomSelf getByRoomId(Long roomId) {
        return lambdaQuery()
                .eq(ChatRoomSelf::getRoomId, roomId)
                .one();
    }


    public Map<String, Long> getFriednRoomIdMap(String uid, List<String> friendUidList) {
        return lambdaQuery()
                .eq(ChatRoomSelf::getUid1, uid)
                .in(ChatRoomSelf::getUid2, friendUidList)
                .or()
                .eq(ChatRoomSelf::getUid2, uid)
                .in(ChatRoomSelf::getUid1, friendUidList)
                .list()
                .stream()
                .collect(
                        (HashMap<String, Long>::new),
                        (m, v) -> m.put(v.getUid1().equals(uid) ? v.getUid2() : v.getUid1(), v.getRoomId()),
                        HashMap::putAll
                );
    }
}
