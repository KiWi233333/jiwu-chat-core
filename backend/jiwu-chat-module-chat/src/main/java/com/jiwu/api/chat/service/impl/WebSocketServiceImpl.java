package com.jiwu.api.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.chat.common.websocket.NettyUtil;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.config.thread.ThreadPoolConfig;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.service.auth.JWTUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.chat.common.dto.WSAuthorize;
import com.jiwu.api.chat.common.event.UserOfflineEvent;
import com.jiwu.api.chat.common.event.UserOnlineEvent;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.chat.service.adapter.WsAdapter;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Description: websocket处理类
 * Date: 2023-03-19 16:21
 */
@Service
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private UserCache userCache;

    @Resource
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private static final Duration EXPIRE_TIME = Duration.ofHours(1);// 有效期
    private static final Long MAX_MUM_SIZE = 10000L;// 最大连接数
    /**
     * 所有请求登录的code与channel关系
     */
    public static final Cache<Integer, Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRE_TIME)
            .maximumSize(MAX_MUM_SIZE)
            .build();
    /**
     * 所有已连接的websocket连接列表和一些额外参数
     */
    private static final ConcurrentHashMap<Channel, UserTokenDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();
    /**
     * 所有在线的用户和对应的socket
     */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Channel, UserTokenDTO> getOnlineMap() {
        return ONLINE_WS_MAP;
    }


    /**
     * 处理所有ws连接的事件（记录连接）
     *
     * @param channel 连接
     */
    @Override
    public void connect(Channel channel, UserTokenDTO dto) {
        ONLINE_WS_MAP.put(channel, dto);
    }

    /**
     * 处理ws断开连接的事件
     *
     * @param channel 连接
     */
    @Override
    public void removed(Channel channel) {
        UserTokenDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        Optional<String> uidOptional = Optional.ofNullable(wsChannelExtraDTO)
                .map(UserTokenDTO::getId);
        boolean isOffline = offline(channel, uidOptional);
        // 判断身份在线
        if (uidOptional.isPresent() && isOffline) {// 已登录用户断连,并且全下线成功
            User user = new User();
            user.setId(uidOptional.get());
            user.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus()); // 离线
            user.setLastLoginTime(new Date()); // 更新最后登录
            // 2、发送 离线消息
            applicationEventPublisher.publishEvent(new UserOfflineEvent(this, user));
        }
        channel.close();
    }

    /**
     * 主动认证登录
     *
     * @param channel 连接
     * @param dto     身份信息
     */
    @Override
    public UserTokenDTO authorize(Channel channel, WSAuthorize dto) {
        Result<Object> result = null;
        if (!StringUtils.isNotBlank(dto.getToken())) {
            result = Result.fail(ResultStatus.TOKEN_ERR);
            sendMsg(channel, result);
            channel.close();
            return null;
        }
        UserTokenDTO userTokenDTO = null;
        // 1、解析token
        try {
            userTokenDTO = JWTUtil.getTokenInfoByToken(dto.getToken());
            if (userTokenDTO == null || StringUtils.isBlank(userTokenDTO.getId())) {
                result = Result.fail(ResultStatus.NULL_ERR);
                sendMsg(channel, result);
                channel.close();
            }
            // 3、设备验证
            if (userTokenDTO != null && !checkAuthHeaders(channel, userTokenDTO)) {
                result = Result.fail(ResultStatus.TOKEN_DEVICE_ERR);
            }
        } catch (TokenExpiredException e1) {
            result = Result.fail(ResultStatus.TOKEN_EXPIRED_ERR);
            sendMsg(channel, result);
        } catch (IOException e) {
            // 3、登录错误
            log.info("身份错误 {}", e.getMessage());
            result = Result.fail(ResultStatus.TOKEN_ERR);
            sendMsg(channel, result);
        }
        if (result != null) {
            log.info("用户聊天验证登录，{}", result.getMessage());
            return null;
        }
        // 保存userId
        NettyUtil.setAttr(channel, NettyUtil.ID, userTokenDTO.getId());
        NettyUtil.setAttr(channel, NettyUtil.USER_AGENT, userTokenDTO.getUa());
        //发送登录成功相关事件
        loginSuccess(channel, userCache.getUserInfo(userTokenDTO.getId()));
        return userTokenDTO;
    }


    /**
     * 设备鉴权
     *
     * @param channel      连接
     * @param userTokenDTO 解析用户信息
     */
    private boolean checkAuthHeaders(Channel channel, UserTokenDTO userTokenDTO) {
        if (StringUtil.isNullOrEmpty(userTokenDTO.getUa())) {
            channel.close();// 关闭连接
            return false;
        }
        // 校验用户是否登录
        // String uaMd5 = MD5Encoder.encode(userAgent.getBytes());
        if (!redisUtil.hExists(UserConstant.USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(), userTokenDTO.getUa())) {
            log.info("身份验证错误，登录设备有误！");
            sendMsg(channel, Result.fail(ResultStatus.TOKEN_DEVICE_ERR));
            channel.close();
            return false;
        }
        return true;
    }

    /**
     * (channel必在本地)登录成功，并更新状态
     */
    private void loginSuccess(Channel channel, User user) {
        //更新上线列表
        online(channel, user.getId());
        //发送给对应的用户
        sendMsg(channel, Result.ok(WsAdapter.buildLoginSuccessVO(user)));
        //发送用户上线事件
        boolean online = userCache.isOnline(user.getId());
        if (!online) { //  设备刚上线，就通知大家
            user.setLastLoginTime(new Date());
            user.setLastLoginIp(NettyUtil.getAttr(channel, NettyUtil.IP));
            user.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
            applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
        }
    }

    /**
     * 用户上线
     */
    private void online(Channel channel, String uid) {
        getOrInitChannelExt(channel).setId(uid);
        ONLINE_UID_MAP.putIfAbsent(uid, new CopyOnWriteArrayList<>());
        ONLINE_UID_MAP.get(uid).add(channel);
        NettyUtil.setAttr(channel, NettyUtil.ID, uid);
    }

    /**
     * 如果在线列表不存在，就先把该channel放进在线列表
     *
     * @param channel channel
     * @return 参数
     */
    private UserTokenDTO getOrInitChannelExt(Channel channel) {
        UserTokenDTO dto = ONLINE_WS_MAP.getOrDefault(channel, new UserTokenDTO());
        UserTokenDTO old = ONLINE_WS_MAP.putIfAbsent(channel, dto);
        return ObjectUtil.isNull(old) ? dto : old;
    }

    /**
     * 推动消息给所有在线的人
     *
     * @param wsBaseVO 发送的消息体
     * @param skipUid  需要跳过的人
     */
    @Override
    public void sendToAllOnline(WsBaseVO<?> wsBaseVO, String skipUid) {
        ONLINE_WS_MAP.forEach((channel, ext) -> {
            if (Objects.nonNull(skipUid) && Objects.equals(ext.getId(), skipUid)) {
                return;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(channel, Result.ok(wsBaseVO)));
        });
    }

    /**
     * 推动消息给所有在线的人
     *
     * @param wsBaseVO 发送的消息体
     */
    @Override
    public void sendToAllOnline(WsBaseVO<?> wsBaseVO) {
        sendToAllOnline(wsBaseVO, null);
    }

    /**
     * 推动消息给指定的人
     *
     * @param wsBaseVO 发送的消息体
     * @param uidList  接收人的id列表
     */
    @Override
    public void sendToUidList(WsBaseVO<?> wsBaseVO, List<String> uidList) {
        uidList.forEach(uid -> sendToUid(wsBaseVO, uid));
    }

    // 发送消息
    private ChannelFuture sendMsg(Channel channel, Result<Object> data) {
        return channel.writeAndFlush(Result.wsRes(data));
    }

    /**
     * 用户下线
     * return 是否全下线成功
     */
    private boolean offline(Channel channel, Optional<String> uidOptional) {
        ONLINE_WS_MAP.remove(channel);
        if (uidOptional.isPresent()) {
            CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uidOptional.get());
            if (CollUtil.isNotEmpty(channels)) {
                channels.removeIf(ch -> Objects.equals(ch, channel));
            }
            return CollUtil.isEmpty(ONLINE_UID_MAP.get(uidOptional.get()));
        }
        return true;
    }


    @Override
    public void sendToUid(WsBaseVO<?> wsBaseVO, String uid) {
        CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uid);
        if (CollUtil.isEmpty(channels)) {
            log.info("用户：{}不在线", uid);
            return;
        }
        channels.forEach(channel -> threadPoolTaskExecutor.execute(() -> sendMsg(channel, Result.ok(wsBaseVO))));
    }

    /**
     * 退出登录
     *
     * @param dto 用户id
     * @return boolean
     */
    @Override
    public boolean logout(UserTokenDTO dto, Boolean isAll) {
        CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(dto.getId());
        if (CollUtil.isEmpty(channels)) {
            log.info("用户：{}不在线", dto.getId());
            return false;
        }
        if (isAll != null && isAll.equals(Boolean.TRUE)) {// 多人
            for (Channel channel : channels) {
                this.removed(channel);
            }
        } else {// 单人
            for (Channel channel : channels) {
                String ua = NettyUtil.getAttr(channel, NettyUtil.USER_AGENT);
                if (!StringUtil.isNullOrEmpty(ua) && ua.equals(dto.getUa())) {
                    this.removed(channel);
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * 是否在线
     *
     * @param uid 用户id
     * @return boolean
     */
    @Override
    public boolean isOnline(String uid) {
        return ONLINE_UID_MAP.containsKey(uid);
    }

    /**
     * 获取在线人数
     *
     * @return int
     */
    @Override
    public int getOnlineCount() {
        return ONLINE_WS_MAP.size();
    }
}
