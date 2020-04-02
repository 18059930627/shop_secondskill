package com.qf.handler;

import com.qf.entity.WsMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

/**
 * 心跳处理器，给客户端发送心跳
 *
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Component
@ChannelHandler.Sharable
public class WebSocketHeartHandler extends SimpleChannelInboundHandler<WsMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WsMsg msg) throws Exception {
        //处理心跳事件
        if (msg.getType() == 2) {
            //返回一个消息
            ctx.writeAndFlush(msg);
        } else {
            //如果不是心跳信息，继续往后处理
            ctx.fireChannelRead(msg);
        }

    }
}
