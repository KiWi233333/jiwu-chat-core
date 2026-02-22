package com.jiwu.api.chat.common.websocket;

import com.jiwu.api.common.constant.JwtConstant;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Description: netty工具类
 */
public class NettyUtil {

    //    public static AttributeKey<String> SEC_PROTOCOL_TOKEN = AttributeKey.valueOf("Sec-WebSocket-Protocol");
    public static AttributeKey<String> TOKEN = AttributeKey.valueOf(JwtConstant.HEADER_NAME);
    public static AttributeKey<String> IP = AttributeKey.valueOf("ip");
    public static AttributeKey<String> ID = AttributeKey.valueOf("id");
    public static AttributeKey<String> USER_AGENT = AttributeKey.valueOf("user-agent");

    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T data) {
        Attribute<T> attr = channel.attr(attributeKey);
        attr.set(data);
    }

    public static <T> T getAttr(Channel channel, AttributeKey<T> ip) {
        return channel.attr(ip).get();
    }
}
