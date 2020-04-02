package com.qf.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * 对恶意请求进行拦截
 *
 * @Author chenzhongjun
 * @Date 2020/1/1
 */
@Component
public class KillFilterRegister extends AbstractGatewayFilterFactory {

    @Autowired
    private KillFilter killFilter;

    /**
     * 过滤器名
     *
     * @return
     */
    @Override
    public String name() {
        return "myKill";
    }

    @Override
    public GatewayFilter apply(Object config) {
        return killFilter;
    }
}
