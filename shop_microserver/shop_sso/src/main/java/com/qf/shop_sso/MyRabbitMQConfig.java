package com.qf.shop_sso;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Configuration
public class MyRabbitMQConfig {

    @Bean
    public FanoutExchange getExchange() {
        return new FanoutExchange("mail_exchange");
    }
}
