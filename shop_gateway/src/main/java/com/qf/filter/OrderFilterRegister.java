package com.qf.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 避免订单的重复提交
 *
 * @Author chenzhongjun
 * @Date 2020/1/3
 */
@Component
public class OrderFilterRegister extends AbstractGatewayFilterFactory implements Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 过滤编码重复下单
     *
     * @param config
     * @return
     */
    @Override
    public GatewayFilter apply(Object config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String token = exchange.getRequest().getQueryParams().getFirst("token");
                //判断redis中是否存在
                String flag = redisTemplate.opsForValue().get(token);
                System.out.println(flag);
                if (flag != null) {
                    //第一次下单
                    //删除redis
                    redisTemplate.delete(token);
                    //放行
                    return chain.filter(exchange);
                }

                //重复下单,返回页面
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SEE_OTHER); //重定向
                String msg = null;
                try {
                    msg = URLEncoder.encode("不可以重复提交订单", "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //设置重定向的位置
                response.getHeaders().set("Location", "/info/error?msg=" + msg);

                return response.setComplete();
            }
        };
    }

    /**
     * 注册名
     *
     * @return
     */
    @Override
    public String name() {
        return "oneOrder";
    }

    /**
     * 过滤的优先级
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 300;
    }
}
