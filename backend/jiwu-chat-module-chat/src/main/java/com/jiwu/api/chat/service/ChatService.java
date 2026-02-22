package com.jiwu.api.chat.service;


import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.req.ChatMessagePageDTO;
import com.jiwu.api.common.main.dto.chat.req.ChatMessageReadDTO;
import com.jiwu.api.common.main.dto.chat.req.SelectGroupMemberPageDTO;
import com.jiwu.api.chat.common.vo.ChatMemberStatisticVO;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.chat.common.vo.ChatMessageVO;
import com.jiwu.api.common.main.dto.chat.vo.ChatMessageReadVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;

import java.util.List;

public interface ChatService {
    /**
     * 游标查询
     *
     * @param dto    参数
     * @param userId 用户id
     * @return true
     */
    CursorPageBaseVO<ChatMessageVO> getMsgPage(ChatMessagePageDTO dto, String userId);

    /**
     * 发送消息
     *
     * @param dto    参数
     * @param userId 用户id
     * @return 消息id
     */
    Long sendMsg(ChatMessageDTO dto, String userId);

    /**
     * 返回消息所有物料
     *
     * @param msgId  消息id
     * @param userId 用户id
     * @return 数据
     */
    ChatMessageVO getMsgDetail(Long msgId, String userId);

    /**
     * 撤回消息
     *
     * @param userId 用户id
     * @param roomId 房间id
     * @param msgId  消息id
     * @return 影响
     */
    Integer recallMsg(String userId, Long roomId, Long msgId);

    /**
     * 获取群聊成员统计信息
     *
     * @return 数据
     */
    ChatMemberStatisticVO getMemberStatistic();


    /**
     * 获取已读未读
     *
     * @param uid 用户id
     * @param dto 参数
     * @return 数据
     */
    CursorPageBaseVO<ChatMessageReadVO> getReadPage(String uid, ChatMessageReadDTO dto);

    /**
     * 获取群成员列表
     *
     * @param memberUidList 用户列表
     * @param dto           参数
     * @return 游标列表
     */
    CursorPageBaseVO<ChatMemberVO> getMemberPage(List<String> memberUidList, SelectGroupMemberPageDTO dto);

    /**
     * 阅读消息
     *
     * @param userId 用户id
     * @param roomId 房间号id
     * @return 阅读行
     */
    Long msgRead(String userId, Long roomId);

    /**
     * 撤回消息
     *
     * @param userId 用户id
     * @param roomId 房间id
     * @param msgId  消息id
     * @return 影响
     */
    Integer deleteMsg(String userId, Long roomId, Long msgId);
}
