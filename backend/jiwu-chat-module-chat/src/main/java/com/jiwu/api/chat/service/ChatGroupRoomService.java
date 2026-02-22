package com.jiwu.api.chat.service;


import com.jiwu.api.common.main.dto.chat.req.AddMemberDTO;
import com.jiwu.api.common.main.dto.chat.req.InsertRoomGroupDTO;
import com.jiwu.api.common.main.dto.chat.req.UpdateRoomGroupDTO;


public interface ChatGroupRoomService {


    /**
     * 添加群聊
     * @param uid 用户id
     * @param dto 参数
     * @return 房间号
     */
    Long addGroup(String uid, InsertRoomGroupDTO dto);

    /**
     * 邀请好友
     * @param uid 用户
     * @param dto 参数
     * @return 影响
     */
    Long addMember(String uid, AddMemberDTO dto);

    /**
     * 删除群成员
     * @param uid 操作人用户id
     * @param roomId 房间id
     * @param targetId 移除id
     * @return 影响
     */
    Integer deleteMember(String uid, Long roomId, String targetId);


    /**
     * 更新群组信息
     * @param id 群组id
     * @param dto 参数
     * @return 影响条数
     */
    Long updateGroup(Long id, UpdateRoomGroupDTO dto);
}
