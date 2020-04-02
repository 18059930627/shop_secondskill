package com.qf.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * 过滤器工厂类
 *
 * @Author chenzhongjun
 * @Date 2020/1/1
 */
@Component
public class CodeFilterRegister extends AbstractGatewayFilterFactory {

    @Autowired
    private CodeFilter codeFilter;

    /**
     * 定义过滤名
     *
     * @return
     */
    @Override
    public String name() {
        return "myCode";
    }

    @Override
    public GatewayFilter apply(Object config) {
        return codeFilter;
    }
}
