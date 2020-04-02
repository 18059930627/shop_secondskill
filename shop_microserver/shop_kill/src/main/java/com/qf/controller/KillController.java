package com.qf.controller;

import com.alibaba.fastjson.JSON;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.qf.aop.IsLogin;
import com.qf.aop.UserHolder;
import com.qf.entity.Goods;
import com.qf.entity.ResultData;
import com.qf.entity.User;
import com.qf.feign.GoodsFeign;
import com.qf.util.DateUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author chenzhongjun
 * @Date 2019/12/28
 */
@Controller
@RequestMapping("kill")
public class KillController {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 验证码工具对象
     */
    @Autowired
    private DefaultKaptcha defaultKaptcha;

    /**
     * 判断库存的lua脚本
     */
    private String lua = "--判断商品库存是否足够\n" +
            "local gid = KEYS[1]\n" +
            "local gnumber = tonumber(ARGV[1])\n" +
            "local uid = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "--当前的商品库存\n" +
            "local gsave = tonumber(redis.call('get','gsave_'..gid))\n" +
            "if gsave <gnumber then\n" +
            "    --库存不足 \n" +
            "    return 0\n" +
            "end\n" +
            "--减库存，返回的是减完后的库存\n" +
            "local nowSave = redis.call('decrby','gsave_'..gid,gnumber)\n" +
            "--排队\n" +
            "redis.call('zadd','paidui_'..gid,now,uid)\n" +
            "return 1";


    /**
     * 得到秒杀商品时间的场次
     *
     * @return
     */
    @RequestMapping("queryKillTime")
    @ResponseBody
    public List<Date> queryKillTime() {
        List<Date> list = new ArrayList<>();
        Date date = DateUtil.getNextNDate(0);
        Date date1 = DateUtil.getNextNDate(1);
        Date date2 = DateUtil.getNextNDate(2);
        list.add(date);
        list.add(date1);
        list.add(date2);
        return list;
    }

    /**
     * 获取当前场次的秒杀商品信息
     *
     * @param n
     * @return
     */
    @RequestMapping("queryKillList")
    @ResponseBody
    public List<Goods> queryKillList(Integer n) {
        Date date = DateUtil.getNextNDate(n);
        //调用商品服务
        List<Goods> goodsList = goodsFeign.queryKillList(date);
        return goodsList;
    }

    /**
     * 获取当前系统时间
     *
     * @return
     */
    @RequestMapping("getSystemTime")
    @ResponseBody
    public ResultData<Date> getSystemTime() {
        return new ResultData<Date>().setCode(ResultData.ResultCodeList.OK).setData(new Date());
    }

    /**
     * 抢购秒杀商品
     * 1、判断gid是否在当前的秒杀场次
     * 2、核对验证码：处理恶意请求，延长用户请求时间
     *
     * @param gid
     * @return
     */
    @RequestMapping("qiangGou")
    @IsLogin(mustLogin = true)
    public String qiangGou(Integer gid, Integer gnumber, ModelMap modelMap) {


        if (gnumber == null) {
            gnumber = 1;
        }

        //获得用户信息
        User user = UserHolder.getUser();

        //利用redis判断库存并且进行排队  now越小，排队越前
        long result = redisTemplate.execute(
                new DefaultRedisScript<>(lua, Long.class),
                Collections.singletonList(gid + ""),
                gnumber + "",
                user.getId() + "",
                System.currentTimeMillis() + "");

        if (result == 0) {
            //库存不足
            return "fail";
        }

        //抢购成功，利用rabbitMQ让商品服务和订单服务消费消息
        //定义一个map，存发送的消息
        Map<String, Object> map = new HashMap<>();
        map.put("gid", gid);
        map.put("gnumber", gnumber);
        map.put("uid", user.getId()); //用户id -> 订单消息
        String uuid = UUID.randomUUID().toString();
        map.put("uuid", uuid);

        //将uuid存放到redis中，用来判断订单服务重复消费相同信息
        redisTemplate.opsForValue().set(uuid, gid + "");

        //调用商品服务，分布式事务
        goodsFeign.updateKillSave(gid, gnumber);

        //将消息发送给订单服务
        rabbitTemplate.convertAndSend("kill_goods_exchange", "", map);

        //等待页面,将gid传到前台
        modelMap.put("gid", gid);

        return "paidui";
    }

    /**
     * 生成验证码，返回给前台，验证码使用cookie保存
     */
    @RequestMapping("code")
    public void code(HttpServletResponse response) {

        //验证码的文本
        String text = defaultKaptcha.createText();

        //根据验证码的文本生成图片
        BufferedImage image = defaultKaptcha.createImage(text);

        //用户的唯一标识符
        String codeToken = UUID.randomUUID().toString();
        //保存到redis中
        redisTemplate.opsForValue().set(codeToken, text, 1, TimeUnit.MINUTES);
        //添加到cookie中
        Cookie cookie = new Cookie("codeToken", codeToken);
        cookie.setMaxAge(60);//一分钟的有效时间
        cookie.setPath("/");
        response.addCookie(cookie);

        //将图片传到浏览器端显示
        try {
            ImageIO.write(image, "jpg", response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取对应用户的排名
     *
     * @param gid
     * @return
     */
    @RequestMapping("getRank")
    @ResponseBody
    @IsLogin
    public ResultData<String> getRank(Integer gid) {
        User user = UserHolder.getUser();
        //获取排名
        Long rank = redisTemplate.opsForZSet().rank("paidui_" + gid, user.getId() + "");
        if (rank == null) {
            //没有排名，说明订单已经下单，删除排队了
            return new ResultData<String>().setCode(ResultData.ResultCodeList.OK).setData("抢购成功！");
        }
        //当前还在排队中
        return new ResultData<String>().setCode(ResultData.ResultCodeList.ERROR).setMsg((rank + 1) + "");
    }

    /**
     * 避免重复下单的Token
     *
     * @return
     */
    @RequestMapping("/getToken")
    @ResponseBody
    public ResultData<String> getToken() {
        String token = UUID.randomUUID().toString();
        //保存到redis中
        redisTemplate.opsForValue().set(token, "11");
        return new ResultData<String>().setCode(ResultData.ResultCodeList.OK).setData(token);
    }

    /**
     * 秒杀开始的提醒
     * 将提醒消息存到Redis的有序集合中。将天作为一个集合，如果都存在同一个集合中
     * 那么数据量太大
     * key               score                             value
     * remind_yyMMdd  hhmm(提醒时间提早10分钟)     (map = uid+gid) - > json字符串
     *
     * @param gid
     * @param flag
     * @return
     */
    @IsLogin(mustLogin = true)
    @RequestMapping("setRemind")
    @ResponseBody  //因为加入AOP  如果用户没有登录则无法转换，只能在前端操作。ajax引擎
    public ResultData<String> setRemind(Integer gid, Integer flag) {

        User user = UserHolder.getUser();

        //通过gid得到商品发布时间
        Goods goods = goodsFeign.queryById(gid);
        Date startTime = goods.getGoodsSecondKill().getStartTime();

        //获得商品发布时间前10分钟的时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.set(Calendar.MINUTE, -10);
        //提醒时间
        Date remindTime = calendar.getTime();

        //有序集合的key
        String yyMMdd = DateUtil.date2String(remindTime, "yyMMdd");

        //有序集合的权重
        String hHmm = DateUtil.date2String(remindTime, "HHmm");

        //有序集合的内容
        Map<String, Integer> map = new HashMap<>();
        map.put("gid", gid);
        map.put("uid", user.getId());

        if (flag == 0) {
            //取消提醒
            redisTemplate.opsForZSet().remove("remind_" + yyMMdd, JSON.toJSONString(map));
        } else {
            //设置提醒
            redisTemplate.opsForZSet().add("remind_" + yyMMdd,
                    JSON.toJSONString(map),
                    Double.valueOf(hHmm));
        }

        return new ResultData<String>().setCode(ResultData.ResultCodeList.OK);
    }

}
