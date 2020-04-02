package com.qf.shop_kill;

import com.qf.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Date;

@SpringBootTest
class ShopKillApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String lua = "local gid = ARGV[1] " +
            "local times  = redis.call('get','kill_goods_now') " +
            "local flag = 0 " +
            "if times then " +
            "flag = redis.call('sismember','kill_goods_'..times,gid) " +
            "end " +
            "return flag";

    @Test
    void contextLoads() {
        Long result = redisTemplate.execute(new DefaultRedisScript<>(lua, Long.class), null, 7 + "");
        System.out.println(result);

        /**
         * 不需要等待前面的结果，直接运行redis命令
         */
        redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                return null;
            }
        });
    }

}
