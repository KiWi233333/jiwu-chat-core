package com.jiwu.api.chat.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.chat.common.vo.ws.*;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.chat.common.dto.ChatMsgDeleteDTO;
import com.jiwu.api.chat.common.dto.ChatMsgRecallDTO;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.common.vo.ChatMemberStatisticVO;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.chat.common.vo.ChatMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Description: ws消息适配器
 * Date: 2023-03-19
 */
@Component
public class WsAdapter {

    @Autowired
    private ChatService chatService;

    /**
     * 构建默认消息
     */
    public static WsBaseVO<?> buildMsgSend(ChatMessageVO msgResp) {
        WsBaseVO<ChatMessageVO> wsBaseVO = new WsBaseVO<>();
        wsBaseVO.setType(WsRespTypeEnum.MESSAGE.getType()); // 新消息
        wsBaseVO.setData(msgResp);
        return wsBaseVO;
    }


    /**
     * 构建测回消息体
     *
     * @param recallDTO 撤回参数
     * @return 推送数据
     */
    public static WsBaseVO<?> buildMsgRecall(ChatMsgRecallDTO recallDTO) {
        WsBaseVO<WSMsgRecall> wsBaseVO = new WsBaseVO<>();
        wsBaseVO.setType(WsRespTypeEnum.RECALL.getType());
        WSMsgRecall recall = new WSMsgRecall();
        BeanUtils.copyProperties(recallDTO, recall);
        wsBaseVO.setData(recall);
        return wsBaseVO;
    }



    /**
     * 构建删除消息体
     *
     * @param dto 撤回参数
     * @return 推送数据
     */
    public static WsBaseVO<?> buildMsgDelete(ChatMsgDeleteDTO dto) {
        WsBaseVO<WSMsgDelete> wsBaseVO = new WsBaseVO<>();
        wsBaseVO.setType(WsRespTypeEnum.DELETE.getType());
        WSMsgDelete delete = new WSMsgDelete();
        BeanUtils.copyProperties(dto, delete);
        wsBaseVO.setData(delete);
        return wsBaseVO;
    }

    /**
     * 构建用户上线消息体
     *
     * @param user 用户
     * @return 响应
     */
    public WsBaseVO<WSOnlineOfflineNotify> buildOnlineNotifyVO(User user) {
        WsBaseVO<WSOnlineOfflineNotify> vo = new WsBaseVO<>();
        // 设置上线
        vo.setType(WsRespTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify notify = new WSOnlineOfflineNotify();
        // 在线消息
        notify.setChangeList(Collections.singletonList(ChatMemberVO.build(user)));
        // 组装消息数量
        assembleNum(notify);
        vo.setData(notify);
        return vo;
    }

    /**
     * 构建用户下下下下线消息体
     *
     * @param user 用户
     * @return 响应
     */
    public WsBaseVO<WSOnlineOfflineNotify> buildOfflineNotifyVO(User user) {
        WsBaseVO<WSOnlineOfflineNotify> wsBaseVO = new WsBaseVO<>();
        wsBaseVO.setType(WsRespTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify onlineOfflineNotify = new WSOnlineOfflineNotify();
        onlineOfflineNotify.setChangeList(Collections.singletonList(buildOfflineInfo(user)));
        assembleNum(onlineOfflineNotify);
        wsBaseVO.setData(onlineOfflineNotify);
        return wsBaseVO;
}

    // 统计群聊成员人数
    private void assembleNum(WSOnlineOfflineNotify onlineOfflineNotify) {
        ChatMemberStatisticVO memberStatistic = chatService.getMemberStatistic();
        onlineOfflineNotify.setOnlineNum(memberStatistic.getOnlineNum());
    }

    // 统计群聊成员人数
    private static ChatMemberVO buildOfflineInfo(User user) {
        ChatMemberVO info = new ChatMemberVO();
        BeanUtil.copyProperties(user, info);
        info.setUserId(user.getId());
        info.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
        info.setNickName(user.getNickname());
        info.setUsername(user.getUsername());
        info.setLastOptTime(user.getLastLoginTime());
        return info;
    }

    // 构建登录成功消息体
    public static WsBaseVO<WSLoginSuccess> buildLoginSuccessVO(User user) {
        WsBaseVO<WSLoginSuccess> wsBaseVO = new WsBaseVO<>();
        wsBaseVO.setType(WsRespTypeEnum.LOGIN_IN.getType());
        WSLoginSuccess wsLoginSuccess = WSLoginSuccess.builder()
                .avatar(user.getAvatar())
                .name(user.getNickname())
                .uid(user.getId())
                .build();
        wsBaseVO.setData(wsLoginSuccess);
        return wsBaseVO;
    }


    // 构建好友申请成功
    public static WsBaseVO<WSFriendApply> buildApplySend(WSFriendApply resp) {
        WsBaseVO<WSFriendApply> wsBaseVO = new WsBaseVO<>();
        wsBaseVO.setType(WsRespTypeEnum.APPLY.getType());
        wsBaseVO.setData(resp);
        return wsBaseVO;
    }
}


//    public static WsBaseVO<WSLoginUrl> buildLoginResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
//        WsBaseVO<WSLoginUrl> wsBaseVO = new WsBaseVO<>();
//        wsBaseVO.setType(WsRespTypeEnum.LOGIN_URL.getType());
//        wsBaseVO.setData(WSLoginUrl.builder().loginUrl(wxMpQrCodeTicket.getUrl()).build());
//        return wsBaseVO;
//    }


//    public static WsBaseVO buildScanSuccessResp() {
//        WsBaseVO wsBaseVO = new WsBaseVO<>();
//        wsBaseVO.setType(WsRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
//        return wsBaseVO;
//    }


//    private void assembleNum(WSOnlineOfflineNotify onlineOfflineNotify) {
//        ChatMemberStatisticResp memberStatistic = chatService.getMemberStatistic();
//        onlineOfflineNotify.setOnlineNum(memberStatistic.getOnlineNum());
//    }

//
//    private static ChatMemberVO buildOfflineInfo(User user) {
//        ChatMemberVO info = new ChatMemberVO();
//        BeanUtil.copyProperties(user, info);
//        info.setUserId();(user.getId());
//        info.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
//        info.setLastOptTime(user.getLastOptTime());
//        return info;
//    }
//
//    public static WsBaseVO<WSLoginSuccess> buildInvalidateTokenResp() {
//        WsBaseVO<WSLoginSuccess> wsBaseVO = new WsBaseVO<>();
//        wsBaseVO.setType(WsRespTypeEnum.TOKEN_EXPIRED_ERR.getType());
//        return wsBaseVO;
//    }
//
//    public static WsBaseVO<ChatMessageVO> buildMsgSend(ChatMessageVO msgResp) {
//        WsBaseVO<ChatMessageVO> wsBaseVO = new WsBaseVO<>();
//        wsBaseVO.setType(WsRespTypeEnum.MESSAGE.getType());
//        wsBaseVO.setData(msgResp);
//        return wsBaseVO;
//    }
//
//    public static WsBaseVO<WSMsgMark> buildMsgMarkSend(ChatMessageMarkDTO dto, Integer markCount) {
//        WSMsgMark.WSMsgMarkItem item = new WSMsgMark.WSMsgMarkItem();
//        BeanUtils.copyProperties(dto, item);
//        item.setMarkCount(markCount);
//        WsBaseVO<WSMsgMark> wsBaseVO = new WsBaseVO<>();
//        wsBaseVO.setType(WsRespTypeEnum.MARK.getType());
//        WSMsgMark mark = new WSMsgMark();
//        mark.setMarkList(Collections.singletonList(item));
//        wsBaseVO.setData(mark);
//        return wsBaseVO;
//    }
