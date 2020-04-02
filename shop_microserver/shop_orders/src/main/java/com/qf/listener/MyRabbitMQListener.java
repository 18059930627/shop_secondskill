package com.qf.listener;

import com.qf.service.IOrderService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @Author chenzhongjun
 * @Date 2020/1/2
 */
@Component
public class MyRabbitMQListener {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 订单服务消费抢购商品添加订单信息
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(name = "kill_goods_exchange", type = "fanout"),
                    value = @Queue(name = "kill_orders_queue", durable = "true")
            )
    )
    public void handlerMsg(Map<String, Object> map, Channel channel, Message message) {

        System.out.println("订单服务受到的消息为:" + map);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {

            //获得uuid
            String uuid = (String) map.get("uuid");
            //通过redis判断uuid，如果存在该uuid则说明没有重复消费信息（删除uuid),否则重复消费信息
            String flag = redisTemplate.opsForValue().get(uuid);
            //没有重复消费
            if (flag != null) {
                //获得商品id、库存、用户id
                Integer gid = (Integer) map.get("gid");
                Integer gnumber = (Integer) map.get("gnumber");
                Integer uid = (Integer) map.get("uid");

                //添加订单消息
                orderService.insertOrders(gid, gnumber, uid);

                //已经购买到了，删除该用户的排队信息
                redisTemplate.opsForZSet().remove("paidui_" + gid, uid + "");

                //删除uuid
                redisTemplate.delete(uuid);

                //手动确认
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                //重复消费，拒绝消息为死信消息
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }

        } catch (IOException e) {
            //抛异常，拒绝消息，将消息重新放回到队头,因为数据库添加失败,
            //让订单服务一致重复添加订单，直至添加成功
            try {
                channel.basicNack(
                        message.getMessageProperties().getDeliveryTag(),
                        false,
                        true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
