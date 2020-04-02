package com.qf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netflix.discovery.converters.Auto;
import com.qf.dao.GoodsImageMapper;
import com.qf.dao.GoodsMapper;
import com.qf.dao.GoodsSecondsKillMapper;
import com.qf.entity.Goods;
import com.qf.entity.GoodsImages;
import com.qf.entity.GoodsSecondKill;
import com.qf.service.IGoodsService;
import com.qf.util.DateUtil;
import jdk.nashorn.internal.runtime.regexp.joni.constants.CCSTATE;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@Service
@CacheConfig(cacheNames = "goods")
public class GoodsServiceImpl implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsImageMapper goodsImageMapper;

    @Autowired
    private GoodsSecondsKillMapper secondsKillMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 添加商品
     *
     * @param goods
     * @return
     */
    @Override
    @Transactional
    @CacheEvict(key = "'kill_'+#goods.goodsSecondKill.startTime.time", condition = "#goods.type==2")
    public int insertGoods(Goods goods) {
        //商品
        goodsMapper.insert(goods);

        //添加商品封面图片
        GoodsImages goodsImages = new GoodsImages()
                .setGoodsId(goods.getId())
                .setUrl(goods.getFmUrl())
                .setIscover(1);

        goodsImageMapper.insert(goodsImages);

        //添加商品其他图片
        for (String otherUrl : goods.getOtherUrls()) {
            goodsImages = new GoodsImages()
                    .setGoodsId(goods.getId())
                    .setUrl(otherUrl)
                    .setIscover(0);
            goodsImageMapper.insert(goodsImages);
        }

        //TODO 添加秒杀商品
        if (goods.getType() == 2) {
            GoodsSecondKill goodsSecondKill = goods.getGoodsSecondKill();
            goodsSecondKill.setGid(goods.getId());
            secondsKillMapper.insert(goodsSecondKill);

            //将秒杀的商品id放入redis的set集合中
            //将时间转化为string类型
            String times = DateUtil.date2String(goodsSecondKill.getStartTime(), "yyMMddHH");
            redisTemplate.opsForSet().add("kill_goods_" + times, goods.getId() + "");
        }

        //TODO 通过rabbitMQ将商品信息传给秒杀服务并生成静态页
        rabbitTemplate.convertAndSend("kill_exchange", "", goods);
        return 1;
    }

    /**
     * 查询商品
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<Goods> getGoodsList() {

        //商品信息
        List<Goods> goodsList = goodsMapper.selectList(null);
        if (goodsList != null && goodsList.size() > 0) {
            for (Goods goods : goodsList) {
                //设置商品图片信息
                goods = this.setUrl(goods);
                //TODO 处理秒杀信息
                QueryWrapper queryWrapper1 = new QueryWrapper();
                queryWrapper1.eq("gid", goods.getId());
                GoodsSecondKill goodsSecondKill = secondsKillMapper.selectOne(queryWrapper1);
                goods.setGoodsSecondKill(goodsSecondKill);
            }
        }
        return goodsList;
    }

    /**
     * 查询当前场次的秒杀商品信息
     *
     * @param date
     * @return
     */
    @Override
    @Cacheable(key = "'kill_'+#date.time")//时间的毫秒数
    public List<Goods> queryKillList(Date date) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("start_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        List<Goods> goodsList = new ArrayList<>();
        //获取秒杀商品信息
        List<GoodsSecondKill> killList = secondsKillMapper.selectList(queryWrapper);
        for (GoodsSecondKill secondKill : killList) {
            //获取商品信息
            Goods goods = goodsMapper.selectById(secondKill.getGid());
            goods.setGoodsSecondKill(secondKill);
            //设置商品图片
            goods = this.setUrl(goods);
            goodsList.add(goods);
        }
        return goodsList;
    }


    @Override
    public int updateKillSave(Integer gid, Integer gnumber) {
        System.out.println("商品服务接收:" + gnumber);
        return secondsKillMapper.updateKillSave(gid, gnumber);
    }

    /**
     * 根据id查询商品信息
     *
     * @param gid
     * @return
     */
    @Override
    public Goods queryById(Integer gid) {
        //得到商品对象
        Goods goods = goodsMapper.selectById(gid);

        //设置图片
        goods = this.setUrl(goods);

        //秒杀信息
        if (goods.getType() == 2) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("gid", gid);
            GoodsSecondKill goodsSecondKill = secondsKillMapper.selectOne(queryWrapper);
            goods.setGoodsSecondKill(goodsSecondKill);
        }
        return goods;
    }

    /**
     * 设置商品的图片信息
     *
     * @return
     */
    public Goods setUrl(Goods goods) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("goods_id", goods.getId());
        //查询商品图片信息
        List<GoodsImages> goodsImagesList = goodsImageMapper.selectList(queryWrapper);
        for (GoodsImages goodsImages : goodsImagesList) {
            //将商品图片添加到商品信息中
            if (goodsImages.getIscover() == 1) {
                //添加封面
                goods.setFmUrl(goodsImages.getUrl());
            } else {
                //添加其他图片
                goods.addOtherUrl(goodsImages.getUrl());
            }
        }
        return goods;
    }
}
