package com.jiwu.api.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.dto.chat.friend.*;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.UserInfoPageDTO;
import com.jiwu.api.user.service.AdminUserService;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatUserFriendService;
import com.jiwu.api.chat.common.vo.ChatUserFriendUnReadVO;
import com.jiwu.api.chat.common.vo.PageBaseVO;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendApplyVO;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendCheckVO;
import com.jiwu.api.common.main.vo.chat.ChatUserFriendVO;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * 聊天模块/联系人模块
 *
 * @since 2023-12-16
 */
@RestController
@RequestMapping("/chat/user/friend")
@Slf4j
public class ChatUserFriendController {
    @Resource(name = "chatUserFriendServiceImpl")
    private ChatUserFriendService chatFriendService;
    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/page")
    @Operation(summary = "联系人列表")
    @PortFlowControl(limit = 30, time = 1, timeUnit = TimeUnit.MINUTES)
    public Result<CursorPageBaseVO<ChatUserFriendVO>> friendList(@Valid CursorPageBaseDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.friendPage(uid, dto));
    }

    @PostMapping("/page/{page}/{size}")
    @Operation(summary = "联系人列表（普通分页）")
    @PortFlowControl(limit = 30, time = 1, timeUnit = TimeUnit.MINUTES)
    public Result<IPage<ChatUserFriendVO>> friendListV2(
            @Parameter(description = "页码") @PathVariable Integer page,
            @Parameter(description = "每页个数") @PathVariable Integer size,
            @Valid @RequestBody ChatFriendPageDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.friendPageV2(page, size, uid, dto));
    }

    @Operation(summary = "获取用户列表（用于好友申请）", tags = { "用户模块" })
    @Parameter(name = "token", description = "用户token", required = true, in = ParameterIn.HEADER)
    @PostMapping(value = "/user/{page}/{size}")
    @PortFlowControl(limit = 30, time = 1, timeUnit = TimeUnit.MINUTES)
    Result<Page<User>> getUserInfoPage(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
            @Parameter(description = "页码") @PathVariable Integer page,
            @Parameter(description = "每页个数") @PathVariable Integer size,
            @Valid @RequestBody ChatUserInfoPageDTO dto) {
        UserInfoPageDTO data = chatUserInfoPageToDTO(dto);
        return Result.ok(adminUserService.getUserInfoPage(page, size, data));
    }

    private UserInfoPageDTO chatUserInfoPageToDTO(@Valid ChatUserInfoPageDTO dto) {
            return new UserInfoPageDTO()
                    .setUserId(dto.getUserId())
                    .setIsCustomer(1)
                .setIsSimpleCustomer(1)
                .setKeyWord(dto.getKeyWord());
    }

    @PostMapping("/check")
    @Operation(summary = "批量判断是否是自己好友")
    public Result<ChatUserFriendCheckVO> check(@Valid @RequestBody ChatUserFriendCheckDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.check(uid, dto));
    }

    @PostMapping("/apply")
    @Operation(summary = "申请好友")
    @PortFlowControl(limit = 10, time = 1, timeUnit = TimeUnit.MINUTES)
    // @ReqPermission(name = "申请好友（包括申请机器人）", intro = "好友模块", expression =
    // "chat:user:friend:apply")
    public Result<Integer> apply(@Valid @RequestBody ChatUserFriendApplyDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        AssertUtil.notEqual(dto.getTargetUid(), uid, "不能添加自己为好友！");
        return Result.ok(chatFriendService.apply(uid, dto));
    }

    @GetMapping("/apply/page")
    @Operation(summary = "好友申请列表")
    public Result<PageBaseVO<ChatUserFriendApplyVO>> page(@Valid PageBaseDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.pageApplyFriend(uid, dto));
    }

    @GetMapping("/apply/unread")
    @Operation(summary = "申请未读数")
    public Result<ChatUserFriendUnReadVO> unread(
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.getUnread(uid));
    }

    @PutMapping("/apply")
    @Operation(summary = "同意好友申请")
    public Result<Integer> applyApprove(@Valid @RequestBody ChatUserFriendApproveDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        return Result.ok(chatFriendService.applyApprove(RequestHolderUtil.get().getId(), dto));
    }

    @DeleteMapping("/apply")
    @Operation(summary = "拒绝好友申请")
    public Result<Integer> deleteApply(@Valid @RequestBody ChatUserFriendRejectDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.deleteApply(uid, dto));
    }

    @DeleteMapping("/{targetUid}")
    @Operation(summary = "删除好友")
    @PortFlowControl(limit = 30, time = 1, timeUnit = TimeUnit.MINUTES)
    public Result<Integer> delete(@Valid @PathVariable("targetUid") String targetUid,
            @RequestHeader(name = JwtConstant.HEADER_NAME) @Parameter(description = "用户token") String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatFriendService.deleteFriend(uid, targetUid));
    }

}
