package com.qf.filter;

import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 是否为恶意请求，进行拦截
 *
 * @Author chenzhongjun
 * @Date 2020/1/1
 */
@Component
public class KillFilter implements GatewayFilter, Ordered {


    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * lua脚本
     */
    private String lua = "local gid = ARGV[1] " +
            "local times  = redis.call('get','kill_goods_now') " +
            "local flag = 0 " +
            "if times then " +
            "flag = redis.call('sismember','kill_goods_'..times,gid) " +
            "end " +
            "return flag";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

         /*//查询当前时间应该是哪个场次
        String now = redisTemplate.opsForValue().get("kill_goods_now");
        //查询当前场次下的所有商品id
        boolean flag = false;
        if(now!=null){
            flag = redisTemplate.opsForSet().isMember("kill_goods_" + now, gid + "");
        }
        if(flag){
            //可以去抢购
            return "success";
        }
        //恶意访问
        return "error";
         */

        //获取gid
        ServerHttpRequest request = exchange.getRequest();
        String gid = request.getQueryParams().getFirst("gid");

        //lua脚本查询该id是否在当前的秒杀场次中
        Long result = redisTemplate.execute(new DefaultRedisScript<>(lua, Long.class), null, gid);
        if (result == 1) {
            //在该场次，放行
            return chain.filter(exchange);
        }
        //该商品未在该场次，进行重定向
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER); //进行重定向
        String msg = null;
        try {
            msg = URLEncoder.encode("该商品还未开始抢购", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //重定向的路径放在请求头中
        response.getHeaders().set("Location", "/info/error?msg=" + msg);

        return response.setComplete();
    }

    /**
     * 值越小，优先级越高，则这里验证码先判断过滤，则值比验证码过滤器的值大
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 200;
    }
}
