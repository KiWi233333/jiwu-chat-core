package com.jiwu.api.chat.service;

import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.chat.common.dto.WSAuthorize;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import io.netty.channel.Channel;

import java.util.List;

public interface WebSocketService {


    /**
     * 处理所有ws连接的事件
     *
     * @param channel 连接
     */
    void connect(Channel channel, UserTokenDTO userTokenDTO);

    /**
     * 处理ws断开连接的事件
     *
     * @param channel 连接
     */
    void removed(Channel channel);

    /**
     * 主动认证登录
     *
     * @param channel     连接
     * @param wsAuthorize 身份信息
     */
    UserTokenDTO authorize(Channel channel, WSAuthorize wsAuthorize);


    /**
     * 推动消息给所有在线的人
     *
     * @param wsBaseVO 发送的消息体
     * @param skipUid  需要跳过的人
     */
    void sendToAllOnline(WsBaseVO<?> wsBaseVO, String skipUid);

    /**
     * 推动消息给所有在线的人
     *
     * @param wsBaseVO 发送的消息体
     */
    void sendToAllOnline(WsBaseVO<?> wsBaseVO);

    /**
     * 推动消息给指定的人
     *
     * @param wsBaseVO 发送的消息体
     * @param uidList  接收人的id列表
     */
    void sendToUidList(WsBaseVO<?> wsBaseVO, List<String> uidList);

    void sendToUid(WsBaseVO<?> wsBaseVO, String uid);

    /**
     * 退出登录
     *
     * @param dto 用户
     * @return boolean
     */
    boolean logout(UserTokenDTO dto, Boolean isAll);

    /**
     * 是否在线
     *
     * @param uid 用户id
     * @return boolean
     */
    boolean isOnline(String uid);

    /**
     * 获取在线人数
     *
     * @return int
     */
    int getOnlineCount();
}
