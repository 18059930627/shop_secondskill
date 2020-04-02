package com.qf.shop_kill;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author chenzhongjun
 * @Date 2020/1/3
 */
@Component
public class MyRabbitMQConfig implements RabbitTemplate.ReturnCallback, RabbitTemplate.ConfirmCallback {

    /**
     * <p>
     * * ConfirmCallback  只确认消息是否正确到达 Exchange 中
     * * ReturnCallback   消息没有正确到达队列时触发回调，如果正确到达队列不执行
     * * <p>
     * * 1. 如果消息没有到exchange,则confirm回调,ack=false
     * * 2. 如果消息到达exchange,则confirm回调,ack=true
     * * 3. exchange到queue成功,则不回调return
     * * 4. exchange到queue失败,则回调return
     */

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        //System.out.println("发送失败的消息为:"+new String(message.getBody()));
        //可以获取到exchange到queue失败的消息
        //引入消息补偿机制，写入数据库，加一个定时器，隔一段时间读数据库，继续将消息写入MQ中。
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
