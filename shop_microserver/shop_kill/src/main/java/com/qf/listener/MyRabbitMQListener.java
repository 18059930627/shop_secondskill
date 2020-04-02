package com.qf.listener;

import com.qf.entity.Goods;
import com.rabbitmq.client.Channel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author chenzhongjun
 * @Date 2019/12/28
 */
@Component
public class MyRabbitMQListener {

    @Autowired
    private Configuration configuration;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成静态页并且在redis中添加商品库存
     *
     * @param goods
     */
    @RabbitListener(queuesToDeclare = @Queue(name = "kill_queue"))
    public void handlerMsg(Goods goods, Channel channel, Message message) {

        //添加商品库存
        redisTemplate.opsForValue().set("gsave_" + goods.getId(), goods.getGoodsSecondKill().getKillSave() + "");

        String path = MyRabbitMQListener.class.getResource("/").getPath() + "static/html";
        File file = new File(path);

        //文件夹不存在，就创建该文件夹
        if (!file.exists()) {
            file.mkdirs();
        }

        //静态资源页面的名称为  商品ID.html
        file = new File(file, goods.getId() + ".html");

        try (
                //准备一个静态页面的输出路径
                Writer out = new FileWriter(file);
        ) {
            //生成模板对象
            Template template = configuration.getTemplate("kill.ftlh");

            //准备数据
            Map<String, Object> map = new HashMap<>();
            map.put("goods", goods);

            //生成静态页面
            template.process(map, out);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //手动确认消息
        /*try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
