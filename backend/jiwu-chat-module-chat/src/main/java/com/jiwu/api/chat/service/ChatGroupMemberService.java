package com.jiwu.api.chat.service;

import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.chat.common.dto.MemberAdminAddDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomGroupVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;

import java.util.List;

/**
 * 群聊权限业务
 *
 * @className: GroupMemberService
 * @author: Kiwi23333
 * @description: 群聊权限业务
 * @date: 2023/12/18 20:21
 */
public interface ChatGroupMemberService {
    /**
     * 获取群聊房间分页列表
     *
     * @param dto 参数
     * @param uid 用户id
     * @return 分页列表
     */
    CursorPageBaseVO<ChatRoomGroupVO> getGroupRoomPage(CursorPageBaseDTO dto, String uid);

    /**
     * 校验是否有该角色（群聊权限）
     *
     * @param groupRoleEnum 参数
     * @param roomId        房间号
     * @param uid           用户id
     * @return 权限
     */
    List<ChatGroupMember> hasPermission(GroupRoleEnum groupRoleEnum, Long roomId, String uid);

    /**
     * 退出群聊
     *
     * @param uid    需要退出的用户ID
     * @param roomId 房间号
     */
    Boolean exitGroup(String uid, Long roomId);

    /**
     * 添加管理员
     *
     * @param uid 房主id
     * @param dto 参数
     * @return 影响
     */
    Integer addAdmin(String uid, MemberAdminAddDTO dto);

    /**
     * 撤销管理员
     *
     * @param uid 用户id
     * @param dto 参数
     * @return 影响
     */
    Integer revokeAdmin(String uid, MemberAdminAddDTO dto);

}
