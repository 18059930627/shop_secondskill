package com.qf.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 验证码判定的过滤器
 * <p>
 * GatewayFilter - 路由网关过滤器需要实现的接口，重写filter方法
 * Ordered - 路由网关过滤器顺序的实现接口
 *
 * @Author chenzhongjun
 * @Date 2020/1/1
 */
@Component
public class CodeFilter implements GatewayFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证码过滤
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获得cookie中的codeToken
        ServerHttpRequest request = exchange.getRequest();
        HttpCookie codeToken = request.getCookies().getFirst("codeToken");

        //用户输入的验证码
        String code = request.getQueryParams().getFirst("code");

        if (codeToken != null) {
            //cookie中的uuid
            String token = codeToken.getValue();

            //通过uuid获取服务端存储的验证码
            String redisCode = redisTemplate.opsForValue().get(token);

            //验证码正确
            if (redisCode != null && redisCode.equals(code)) {
                //删除验证码
                redisTemplate.delete(redisCode);
                //放行
                return chain.filter(exchange);
            }
        }

        //验证码错误 - 重定向
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER); //重定向

        String msg = null;

        try {
            msg = URLEncoder.encode("验证码错误", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //设置重定向的位置
        response.getHeaders().set("Location", "/info/error?msg=" + msg);

        return response.setComplete();
    }

    /**
     * 返回当前过滤器的优先级，值越小，优先级越大
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 100;
    }
}
