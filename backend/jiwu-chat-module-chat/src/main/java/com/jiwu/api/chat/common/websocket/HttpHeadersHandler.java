package com.jiwu.api.chat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import com.jiwu.api.common.constant.JwtConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * 首次握手适配器 （Http）
 */
@Slf4j
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.uri());

            // 1、获取token参数
            String token = Optional.ofNullable(urlBuilder.getQuery())
                    .map(k -> k.get(JwtConstant.HEADER_NAME)).map(CharSequence::toString)
                    .orElse("");
            // 2、保存token到netty channel
            NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, token);
            // 3、读取写入IP
            this.parseRequestIP(ctx, request, urlBuilder);
            // 4、连接管道移除
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(request);// 返回原始request
        } else {
            ctx.fireChannelRead(msg);// 返回原始request
        }
    }

    /**
     * 解析请求头
     * @param ctx 上下文
     * @param request 请求
     * @param urlBuilder 构造器
     */
    private void parseRequestIP(ChannelHandlerContext ctx, FullHttpRequest request, UrlBuilder urlBuilder) {
        request.setUri(urlBuilder.getPath().toString());
        HttpHeaders headers = request.headers();
        String ip = headers.get("X-Real-IP");
        if (StringUtils.isEmpty(ip)) {//如果没经过nginx，就直接获取远端地址
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            ip = address.getAddress().getHostAddress();
        }
        NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);
    }
}
