package com.qf.shop_goods;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author chenzhongjun
 * @Date 2019/12/28
 */
@Configuration
public class MyRabbitMQConfig {

    /**
     * 声明交换机
     *
     * @return
     */
    @Bean
    public FanoutExchange getExchange() {
        return new FanoutExchange("kill_exchange");
    }

    /**
     * 声明队列
     *
     * @return
     */
    @Bean
    public Queue getQueue() {
        return new Queue("kill_queue");
    }

    /**
     * 绑定交换机
     *
     * @param getQueue
     * @param getExchange
     * @return
     */
    @Bean
    public Binding toBinding(Queue getQueue, FanoutExchange getExchange) {
        return BindingBuilder.bind(getQueue).to(getExchange);
    }
}
