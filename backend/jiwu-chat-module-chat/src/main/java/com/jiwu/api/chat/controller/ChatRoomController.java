package com.jiwu.api.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.chat.common.dto.MemberAdminAddDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.friend.ChatFriendPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.req.AddMemberDTO;
import com.jiwu.api.common.main.dto.chat.req.InsertRoomGroupDTO;
import com.jiwu.api.common.main.dto.chat.req.SelectGroupMemberPageDTO;
import com.jiwu.api.common.main.dto.chat.req.UpdateRoomGroupDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomGroupVO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomSelfVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.chat.service.ChatGroupMemberService;
import com.jiwu.api.chat.service.ChatGroupRoomService;
import com.jiwu.api.chat.service.ChatRoomService;
import com.jiwu.api.chat.service.ChatUserFriendService;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.chat.common.vo.InsertVO;
import com.jiwu.api.common.main.vo.chat.ChatMemberListVO;
import com.jiwu.api.chat.common.vo.room.ChatRoomInfoVO;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天模块/聊天室模块
 *
 * @since 2023-03-19
 */
@RestController
@RequestMapping("/chat/room")
@Tag(name = "聊天模块/聊天室模块")
@Slf4j
public class ChatRoomController {
    @Resource
    private ChatRoomService roomService;
    @Resource
    /* ******************** 群聊信息 ******************************** */
    private ChatGroupMemberService groupMemberService;

    @GetMapping("/group/page")
    @Operation(summary = "获取群聊房间列表（分页）")
    public Result<CursorPageBaseVO<ChatRoomGroupVO>> getGroupRoomPage(
            @Valid CursorPageBaseDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(groupMemberService.getGroupRoomPage(dto, uid));
    }

    @GetMapping("/group/{id}")
    @Operation(summary = "获取群聊房间详情")
    public Result<ChatRoomInfoVO> groupDetail(@PathVariable Long id,
                                              @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.getGroupDetail(uid, id));
    }


    @PutMapping("/group/{id}")
    @Operation(summary = "更新群聊信息")
    public Result<Long> updateGroup(@Valid @RequestBody UpdateRoomGroupDTO dto,
                                    @PathVariable @Parameter(description = "房间id") Long id,
                                    @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(groupRoomService.updateGroup(id, dto));
    }


    @GetMapping("/group/member/page")
    @Operation(summary = "获取群成员列表")
    public Result<CursorPageBaseVO<ChatMemberVO>> getMemberPage(@Valid SelectGroupMemberPageDTO dto,
                                                                @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(roomService.getGroupMemberPage(dto));
    }

    @GetMapping("/group/member/list/{roomId}")
    @Operation(summary = "房间内的所有群成员列表（@列表）")
    public Result<List<ChatMemberListVO>> getMemberList(@PathVariable @Parameter(description = "房间id") Long roomId,
                                                        @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok(roomService.getMemberList(roomId));
    }

    @DeleteMapping("/group/member/exit/{roomId}")
    @Operation(summary = "退出群聊")
    @PortFlowControl(time = 10, limit = 5, errorMessage = "退出过于频繁，请稍后再试！")
    public Result<Boolean> exitGroup(@Parameter(description = "房间id") @PathVariable Long roomId,
                                     @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(groupMemberService.exitGroup(uid, roomId));
    }

    /********************* 群聊和成员管理 *********************************/
    @Resource
    private ChatGroupRoomService groupRoomService;

    @PostMapping("/group")
    @Operation(summary = "新增群聊")
    public Result<InsertVO> addGroup(@Valid @RequestBody InsertRoomGroupDTO dto,
                                     @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        Long roomId = groupRoomService.addGroup(uid, dto);
        return Result.ok(new InsertVO(roomId));
    }


    @PostMapping("/group/member")
    @Operation(summary = "邀请好友")
    public Result<Long> addMember(@Valid @RequestBody AddMemberDTO dto,
                                  @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(groupRoomService.addMember(uid, dto));
    }

    @DeleteMapping("/group/member/{roomId}/{targetId}")
    @Operation(summary = "移除成员")
    public Result<Integer> delMember(@Parameter(description = "房间号") @PathVariable Long roomId,
                                     @Parameter(description = "目标uid") @PathVariable String targetId,
                                     @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(groupRoomService.deleteMember(uid, roomId, targetId));
    }

    @PutMapping("/group/admin")
    @Operation(summary = "添加管理员")
    public Result<Integer> addAdmin(@Valid @RequestBody MemberAdminAddDTO dto,
                                    @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(groupMemberService.addAdmin(uid, dto));
    }

    @DeleteMapping("/group/admin")
    @Operation(summary = "撤销管理员")
    public Result<Integer> revokeAdmin(@Valid MemberAdminAddDTO dto,
                                       @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(groupMemberService.revokeAdmin(uid, dto));
    }


    @Resource
    /* ******************** 私聊（包括机器人）信息 ******************************** */
    private ChatUserFriendService userFriendService;

    @PostMapping("/self/page")
    @Operation(summary = "联系人房间列表（分页）")
    @PortFlowControl(limit = 30, time = 1, timeUnit = TimeUnit.MINUTES)
    public Result<IPage<ChatRoomSelfVO>> getFriendRoomPage(@RequestBody @Valid ChatFriendPageBaseDTO dto, @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(userFriendService.getFriendRoomPage(dto, uid));
    }

}
