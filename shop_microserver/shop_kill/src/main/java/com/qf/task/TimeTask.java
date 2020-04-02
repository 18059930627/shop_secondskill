package com.qf.task;

import com.alibaba.fastjson.JSON;
import com.netflix.discovery.converters.Auto;
import com.qf.entity.Goods;
import com.qf.entity.WsMsg;
import com.qf.feign.GoodsFeign;
import com.qf.util.DateUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author chenzhongjun
 * 定时器类
 * @Date 2019/12/30
 */
@Component
public class TimeTask {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 设置当前场次的lua脚本
     */
    private String lua =
            "local now = ARGV[1] \n" +
                    "local times = redis.call('get','kill_goods_now') \n" +
                    "if times then \n" +
                    "redis.call('del','kill_goods_'..times) \n" +
                    "end \n" +
                    "redis.call('set','kill_goods_now',now)";

    /**
     * cron表达式 秒 分 时  日 月 [星期] 年
     * 整小时，调用一次
     */
    @Scheduled(cron = "0 0 0/1 * * *")
    public void myTask() {
        //设置redis的当前场次，抢购时通过这个场次来判断商品是否正在当前场次中
        //先删除原来的场次
//        String oldDate = redisTemplate.opsForValue().get("kill_goods_now");
//        if(oldDate!=null && !"".equals(oldDate)){
//            redisTemplate.delete("kill_goods_"+oldDate);
//        }
//        //更新redis中的场次信息
//        String now = DateUtil.date2String(new Date(), "yyMMddHH");
//        //设置有效期为1个小时
//        redisTemplate.opsForValue().set("kill_goods_now",now,1, TimeUnit.HOURS);
        String now = DateUtil.date2String(new Date(), "yyMMddHH");
        redisTemplate.execute(new DefaultRedisScript<>(lua), null, now);
    }

    /**
     * 每个小时的50分钟准时去Redis中找对应的提醒信息（用户ID和商品ID）
     */
    @Scheduled(cron = "0 50 * * * *")
    public void myTask2() {

        //获取当前时间
        Date now = new Date();

        //年月日
        String yyMMdd = DateUtil.date2String(now, "yyMMdd");

        //时分
        String hHmm = DateUtil.date2String(now, "HHmm");

        //通过Redis找到对应的提醒消息,考虑到消息量巨大的原因，因为该秒杀服务需要进行集群
        //则通过限制性的去获取提醒消息，就可以让其他秒杀服务也能发送提醒消息给Netty服务器
        //并且发送的时间大大缩小。因为有删除对应发送的内容，再使用Lua脚本就不会有重复发送消息的可能。
        while (true) {
            //获取当前的内容
            Set<String> contents = redisTemplate.opsForZSet().rangeByScore(
                    "remind_" + yyMMdd,
                    Double.valueOf(hHmm),
                    Double.valueOf(hHmm),
                    0,
                    100);

            //Redis中已经没有数据了
            if (contents == null || contents.size() == 0) {
                break;
            }

            //删除对应读取的Redis数据,value需要为数组
            redisTemplate.opsForZSet().remove("remind_" + yyMMdd, contents.toArray());

            //遍历存放集合中的内容 json -> map
            for (String content : contents) {
                Map<String, Object> map = JSON.parseObject(content, HashMap.class);
                Integer gid = (Integer) map.get("gid");
                Goods goods = goodsFeign.queryById(gid);
                //将商品信息存入Map集合中
                map.put("goods", goods);

                //将消息封装成固定的对象，便于Netty接收不同的数据
                WsMsg wsMsg = new WsMsg()
                        //系统发送
                        .setFromId(-1)
                        //发送给固定的用户
                        .setToId((Integer) map.get("uid"))
                        //发送的类型为秒杀信息
                        .setType(3)
                        //发送的数据为Map集合
                        .setData(map);

                //通过RabbitMQ进行广播发送给Netty服务器
                rabbitTemplate.convertAndSend("netty_exchange", "", wsMsg);
            }

        }
    }
}
