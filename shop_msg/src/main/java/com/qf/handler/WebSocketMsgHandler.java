package com.qf.handler;

import com.alibaba.fastjson.JSON;
import com.qf.entity.WsMsg;
import com.qf.util.ChannelUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

/**
 * 第一个入站处理器，将文本帧转成对象
 *
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Component
@ChannelHandler.Sharable
public class WebSocketMsgHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有一个客户端连接到服务器");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有一个客户端与服务器断开了连接");

        //下线后移除关系
        Integer uid = ChannelUtil.getUid(ctx.channel());
        ChannelUtil.removeChannel(uid);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {

        //信息字符串
        String msg = textWebSocketFrame.text();

        //将json字符串转换为消息对象
        WsMsg wsMsg = JSON.parseObject(msg, WsMsg.class);

        //将wsMsg对象抛给后面的事件处理器进行处理,还需要一个出站处理器，来将WsMsg对象转成文本帧
        ctx.fireChannelRead(wsMsg);
    }
}
