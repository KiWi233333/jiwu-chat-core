package com.jiwu.api.chat.common.websocket;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.jiwu.api.chat.common.dto.WsBaseDTO;
import com.jiwu.api.chat.common.enums.WsReqType;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.chat.common.dto.WSAuthorize;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;

    private RedisUtil<?, ?> redisUtil;

    /**
     * 读取客户端发送的请求报文 （登录等）
     *
     * @param ctx the {@link ChannelHandlerContext} which this
     *            {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        WsBaseDTO wsBaseDTO = null;
        try {
            wsBaseDTO = JSONUtil.toBean(msg.text(), WsBaseDTO.class);
        } catch (Exception e) {
            ctx.channel().writeAndFlush(Result.fail("传入参数错误！"));
        }
        // 用户id
        String userId = NettyUtil.getAttr(ctx.channel(), NettyUtil.ID);
        if (wsBaseDTO != null) {
            WsReqType wsReqTypeEnum = WsReqType.of(wsBaseDTO.getType());
            switch (wsReqTypeEnum) {
                case CHECK_TOKEN:// 1、身份验证
                    log.info("用户：{}登入聊天系统", userId);
                    this.webSocketService.authorize(ctx.channel(), new WSAuthorize(wsBaseDTO.getData()));
                    break;
                case HEARTBEAT:// 2、心跳
                    // log.info("用户心跳，{}", userId);
                    break;
                default:
                    log.info("未知类型");
                    break;
            }
        }
    }

    // 处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("聊天模块，异常发生，异常消息：{}", cause.getMessage());
        ctx.channel().writeAndFlush(Result.wsRes(Result.fail("连接异常，请稍后再试！")));
        ctx.channel().close();
    }

    /**
     * 当web客户端连接后，触发该方法
     *
     * @param ctx 上下文
     * @throws Exception 错误
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.webSocketService = SpringUtil.getBean(WebSocketService.class);
        this.redisUtil = SpringUtil.getBean(RedisUtil.class);
    }

    // 客户端离线
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        userOffLine(ctx);
    }

    /**
     * 取消绑定
     *
     * @param ctx 上下文
     * @throws Exception 错误
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOffLine(ctx); // 离线
        log.info("聊天模块 用户：{} 掉线连接：{}", NettyUtil.getAttr(ctx.channel(), NettyUtil.ID), ctx.channel().id());
    }

    // 用户离线
    private void userOffLine(ChannelHandlerContext ctx) {
        if (ctx.channel() != null) {
            try {
                this.webSocketService.removed(ctx.channel());
            } catch (Exception e) {
                log.error("用户离线处理失败，原因：{}", e.getMessage());
            }
        }
    }

    /**
     * 连接 | 心跳检查
     *
     * @param ctx context
     * @param evt event
     * @throws Exception 错误
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            // 读空闲 -- 断开没聊天就下线 关闭
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                // 关闭用户的连接
                userOffLine(ctx);
            }
        }
        // 2、握手完成 | 可用于鉴权
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            // 1）获取token
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            // 2）鉴权
            UserTokenDTO userTokenDTO = this.webSocketService.authorize(ctx.channel(), new WSAuthorize(token));
            // 3）结果
            if (userTokenDTO != null) {
                this.webSocketService.connect(ctx.channel(), userTokenDTO);
            } else {
                this.webSocketService.removed(ctx.channel());
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 设备鉴权
     *
     * @param channel      连接
     * @param req          请求headers
     * @param userTokenDTO 解析用户信息
     */
    private boolean checkAuthHeaders(Channel channel, HttpHeaders req, UserTokenDTO userTokenDTO) {
        String userAgent = req.get(UserConstant.USER_AGENT_KEY);
        if (StringUtil.isNullOrEmpty(userAgent)) {
            channel.close();// 关闭连接
            return false;
        }
        // 校验用户是否登录
        // String uaMd5 = MD5Encoder.encode(userAgent.getBytes());
        if (!redisUtil.hExists(UserConstant.USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(), userAgent)) {
            log.info("身份验证错误，登录设备有误！");
            channel.writeAndFlush(Result.wsRes(Result.fail(ResultStatus.TOKEN_DEVICE_ERR)));
            channel.close();
            return false;
        }
        return true;
    }

}
