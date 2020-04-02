package com.qf.listener;

import com.netflix.discovery.converters.Auto;
import com.qf.service.IGoodsService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @Author chenzhongjun
 * @Date 2020/1/2
 */
/*@Component*/
public class MyRabbitMQListener {

    @Autowired
    private IGoodsService goodsService;

    /**
     * 商品服务消费抢购商品的MQ
     */
    /*@RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(name = "kill_goods_exchange",type = "fanout"),
                    value = @Queue(name = "kill_goods_queue",durable = "true")
            )
    )*/
    public void msgHandler(Map<String, Object> map, Channel channel, Message message) {

        System.out.println("商品服务接收到消息：" + map);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //获得商品id和库存
        Integer gid = (Integer) map.get("gid");
        Integer gnumber = (Integer) map.get("gnumber");

        //同步修改商品库存
        goodsService.updateKillSave(gid, gnumber);

        //确认消息
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
