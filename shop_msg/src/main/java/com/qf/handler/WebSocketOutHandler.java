package com.qf.handler;

import com.alibaba.fastjson.JSON;
import com.qf.entity.WsMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

/**
 * 出站处理器，将对象->json字符串->文本帧
 *
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Component
@ChannelHandler.Sharable
public class WebSocketOutHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //如果输出时类型为WsMsg对象，则转换成文本帧输出
        if (msg instanceof WsMsg) {
            String text = JSON.toJSONString(msg);
            super.write(ctx, new TextWebSocketFrame(text), promise);
        } else {
            super.write(ctx, msg, promise);
        }

    }
}
