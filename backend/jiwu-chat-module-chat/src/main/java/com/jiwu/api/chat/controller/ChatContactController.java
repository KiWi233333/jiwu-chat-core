package com.jiwu.api.chat.controller;


import com.jiwu.api.common.main.enums.chat.ContactNoticeStatus;
import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.main.dto.chat.contact.ContactPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatRoomService;
import com.jiwu.api.chat.common.vo.ws.WSPinContactMsg;
import com.jiwu.api.chat.common.vo.ws.WSUpdateContactInfoMsg;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * 聊天模块/会话模块
 *
 * @since 2023-03-19
 */
@RestController
@RequestMapping("/chat/contact")
@Tag(name = "聊天模块/会话模块")
@Slf4j
public class ChatContactController {
    @Resource
    private ChatRoomService roomService;

    @GetMapping("/page")
    @Operation(summary = "会话列表")
    public Result<CursorPageBaseVO<ChatRoomVO>> getRoomPage(@Valid ContactPageBaseDTO dto,
                                                            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        // 1、uid
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.getContactPage(dto, uid));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "会话详情")
    public Result<ChatRoomVO> getContactDetail(@Parameter(description = "roomId") @PathVariable Long roomId,
                                               @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        // 1、uid
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.getContactDetail(uid, roomId));
    }

    @GetMapping("/self/{friendUid}")
    @Operation(summary = "会话详情（好友）")
    public Result<ChatRoomVO> getContactDetailByFriend(
            @Parameter(description = "好友id") @PathVariable String friendUid,
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.getContactDetailByFriend(uid, friendUid));
    }

    @GetMapping("/self/room/{roomId}")
    @Operation(summary = "会话详情（好友 房间）")
    public Result<ChatRoomVO> getContactDetailByFriend(
            @Parameter(description = "好友id") @PathVariable Long roomId,
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.getContactDetailByRoom(uid, roomId));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "删除会话")
    @PortFlowControl(limit = 100, time = 1, timeUnit = TimeUnit.HOURS, errorMessage = "请求太频繁，请稍后重试！")
    public Result<Integer> deleteRoom(@Parameter(description = "roomId") @PathVariable Long roomId,
                                      @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();// uid
        return Result.ok(roomService.deleteContact(roomId, uid));
    }

    @PutMapping("/group/restore/{roomId}")
    @Operation(summary = "恢复会话（群聊）")
    public Result<ChatRoomVO> restoreGroupRoom(@Parameter(description = "roomId") @PathVariable Long roomId,
                                               @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.restoreContactByRoomId(roomId, uid));
    }

    @PutMapping("/friend/restore/{friendId}")
    @Operation(summary = "恢复会话（私聊）")
    public Result<ChatRoomVO> restoreFriendRoom(@Parameter(description = "roomId") @PathVariable String friendId,
                                                @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.restoreContactByFriendId(friendId, uid));
    }

    @PutMapping("/pin/{roomId}/{type}")
    @Operation(summary = "置顶会话")
    public Result<WSPinContactMsg> pinRoom(@Parameter(description = "roomId") @PathVariable Long roomId,
                                           @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                           @Parameter(description = "置顶类型") @PathVariable Integer type) {
        AssertUtil.isTrue(type == 0 || type == 1, "置顶类型错误错误！");
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.pinContact(roomId, uid, type));
    }

//    @PutMapping("/notice/{roomId}/{status}")
//    @Operation(summary = "设置会话通知状态")
//    public ResultWSUpdateContactInfoMsg> setNoticeStatus(@Parameter(description = "roomId") @PathVariable Long roomId,
//                                                                    @RequestHeader(name = HEADER_NAME) String token,
//                                                                    @Parameter(description = "通知状态") @PathVariable Integer status) {
//        AssertUtil.isTrue(ContactNoticeStatus.isValid(status), "通知状态错误错误！");
//        String uid = RequestHolderUtil.get().getId();
//        return Result.ok(roomService.setNoticeStatus(roomId, uid, status));
//    }

    @PutMapping("/notice/shield/{roomId}/{shield}")
    @Operation(summary = "免打扰会话")
    public Result<WSUpdateContactInfoMsg> setShieldStatus(@Parameter(description = "roomId") @PathVariable Long roomId,
                                                 @RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                                                 @Parameter(description = "免打扰状态") @PathVariable Integer shield) {
        AssertUtil.isTrue(ContactNoticeStatus.isValid(shield), "免打扰状态错误错误！");
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(roomService.setShieldStatus(roomId, uid, shield));
    }

}

