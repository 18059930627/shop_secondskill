package com.qf.handler;

import com.qf.entity.WsMsg;
import com.qf.util.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 初始化事件处理器，将用户与Channel进行绑定
 *
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Component
@ChannelHandler.Sharable
public class WebSocketInitHandler extends SimpleChannelInboundHandler<WsMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WsMsg msg) throws Exception {
        if (msg.getType() == 1) {
            //得到用户ID
            Integer uid = (Integer) msg.getData();

            //得到Channel
            Channel channel = ctx.channel();

            //绑定 映射关系
            ChannelUtil.add(uid, channel);
        } else {
            //继续往后处理
            ctx.fireChannelRead(msg);
        }
    }
}
