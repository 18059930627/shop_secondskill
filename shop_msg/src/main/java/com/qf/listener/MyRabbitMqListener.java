package com.qf.listener;

import com.qf.entity.WsMsg;
import com.qf.util.ChannelUtil;
import io.netty.channel.Channel;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Component
public class MyRabbitMqListener {


    /**
     * 处理秒杀服务发送过来的提醒消息
     *
     * @param wsMsg
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(name = "netty_exchange", type = "fanout"),
                    value = @Queue(name = "netty_queue_${server.port}", durable = "true")
            )
    )
    public void msgHandler(WsMsg wsMsg) {
        //得到用户ID
        Integer uid = wsMsg.getToId();

        //通过用户ID得到Channel
        Channel channel = ChannelUtil.getChannel(uid);
        //发送消息给Channel，通知秒杀已经开始
        if (channel != null) {
            //输出信息，商品信息在wsMsg的data中
            channel.writeAndFlush(wsMsg);
        } else {
            //可能该用户的连接绑到了其他Net统一服务器
        }
        System.out.println("有一个消息需要发送给id为" + uid + "的用户！！！！");
    }

}
