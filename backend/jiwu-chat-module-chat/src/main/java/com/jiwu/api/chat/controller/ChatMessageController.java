package com.jiwu.api.chat.controller;

import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.reaction.ReactionToggleDTO;
import com.jiwu.api.common.main.dto.chat.req.ChatMessagePageDTO;
import com.jiwu.api.common.main.dto.chat.req.ChatMessageReadDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatMessageReadVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.common.vo.ReactionVO;
import com.jiwu.api.chat.common.vo.ws.WSMsgReaction;
import com.jiwu.api.chat.service.ChatMessageReactionService;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.common.vo.ChatMessageVO;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天模块/消息模块
 *
 * @className: ChatController
 * @author: Kiwi23333
 * @description: 聊天模块/消息模块
 * @date: 2023/12/8 17:14
 */
@RestController
@RequestMapping("/chat/message")
@Tag(name = "聊天模块/消息模块")
@Slf4j
public class ChatMessageController {

    @Resource
    private ChatService chatService;

    @Resource
    private ChatMessageReactionService chatMessageReactionService;

    @GetMapping("/page")
    @Operation(summary = "消息列表")
    public Result<CursorPageBaseVO<ChatMessageVO>> getMsgPage(@Valid ChatMessagePageDTO dto, @RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        // 1、uid
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        // 2、获取
        CursorPageBaseVO<ChatMessageVO> msgPage = chatService.getMsgPage(dto, userId);
        // 3、过滤
        return Result.ok(msgPage);
    }


    @PostMapping("")
    @Operation(summary = "发送消息")
    @PortFlowControl(limit = 60, time = 1, timeUnit = TimeUnit.MINUTES, errorMessage = "发送消息过于频繁（60条/分钟）！")
    public Result<ChatMessageVO> sendMsg(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @Valid @RequestBody ChatMessageDTO dto, HttpServletRequest request) {
        // 1、uid
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        // 校验消息类型
        AssertUtil.isFalse(MessageTypeEnum.checkDisable(dto.getMsgType()), "消息类型不支持！");
        // 2、保存
        Long msgId = chatService.sendMsg(dto, userId);
        // 3、返回完整消息
        final ChatMessageVO vo = chatService.getMsgDetail(msgId, userId);
        vo.setClientId(dto.getClientId());
        return Result.ok("发送成功！", vo);
    }


    @PutMapping("/recall/{roomId}/{id}")
    @Operation(summary = "撤回消息")
    @PortFlowControl(time = 10, limit = 5, errorMessage = "撤回消息过于频繁！")
    public Result<Integer> recallMsg(@PathVariable Long roomId, @PathVariable Long id, @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String recall = RequestHolderUtil.get().getId();
        return Result.ok(chatService.recallMsg(recall, roomId, id));
    }

    @DeleteMapping("/recall/{roomId}/{id}")
    @Operation(summary = "删除消息（违规）")
    @PortFlowControl(time = 10, limit = 5, errorMessage = "删除消息过于频繁！")
    public Result<Integer> deleteMsg(@PathVariable Long roomId, @PathVariable Long id, @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String recall = RequestHolderUtil.get().getId();
        return Result.ok(chatService.deleteMsg(recall, roomId, id));
    }

    @GetMapping("/read/page")
    @Operation(summary = "获取消息的已读未读列表")
    public Result<CursorPageBaseVO<ChatMessageReadVO>> getReadPage(@Valid ChatMessageReadDTO dto, @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String uid = RequestHolderUtil.get().getId();
        return Result.ok(chatService.getReadPage(uid, dto));
    }


    @PutMapping("/msg/read/{roomId}")
    @Operation(summary = "消息阅读上报")
    public Result<Long> msgRead(@Parameter(description = "房间号") @PathVariable Long roomId, @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String userId = RequestHolderUtil.get().getId();
        return Result.ok(chatService.msgRead(userId, roomId));
    }


    // ===================== 表情反应 =====================

    @PutMapping("/msg/{roomId}/reaction")
    @Operation(summary = "Toggle 添加/取消表情反应")
    @PortFlowControl(limit = 30, time = 1, timeUnit = TimeUnit.MINUTES, errorMessage = "表情回应过于频繁！")
    public Result<WSMsgReaction> toggleReaction(
            @Parameter(description = "房间ID") @PathVariable Long roomId,
            @Valid @RequestBody ReactionToggleDTO dto,
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String userId = RequestHolderUtil.get().getId();
        return Result.ok(chatMessageReactionService.toggleReaction(roomId, dto, userId));
    }

    @GetMapping("/msg/{msgId}/reactions")
    @Operation(summary = "查询单条消息的表情反应详情（全量用户列表）")
    public Result<List<ReactionVO>> getReactionDetail(
            @Parameter(description = "消息ID") @PathVariable Long msgId,
            @RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        String userId = RequestHolderUtil.get().getId();
        return Result.ok(chatMessageReactionService.getReactionDetail(msgId, userId));
    }

}
