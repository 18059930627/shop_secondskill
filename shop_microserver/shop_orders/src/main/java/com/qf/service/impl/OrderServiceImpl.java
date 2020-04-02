package com.qf.service.impl;

import com.netflix.discovery.converters.Auto;
import com.qf.dao.OrderDetailsMapper;
import com.qf.dao.OrderMapper;
import com.qf.entity.Goods;
import com.qf.entity.Order;
import com.qf.entity.OrderDetils;
import com.qf.feign.GoodsFeign;
import com.qf.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @Author chenzhongjun
 * @Date 2020/1/2
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private OrderDetailsMapper orderDetailsMapper;

    @Override
    @Transactional
    public int insertOrders(Integer gid, Integer gnumber, Integer uid) {

        //通过gid得到Goods对象
        Goods goods = goodsFeign.queryById(gid);

        System.out.println(goods);

        //创建订单
        Order order = new Order()
                .setUid(uid)
                .setOrderId(UUID.randomUUID().toString())
                .setAllPrice(goods.getGoodsSecondKill().getKillPrice().multiply(BigDecimal.valueOf(gnumber)));

        //保存订单
        orderMapper.insert(order);

        //创建订单详情
        OrderDetils orderDetils = new OrderDetils()
                .setOrderId(order.getId())
                .setFmurl(goods.getFmUrl())
                .setGoodsId(gid)
                .setNumber(gnumber)
                .setPrice(goods.getGoodsSecondKill().getKillPrice())
                .setDetailsPrice(goods.getGoodsSecondKill().getKillPrice().multiply(BigDecimal.valueOf(gnumber)))
                .setGoodsId(gid);

        //保存订单详情
        orderDetailsMapper.insert(orderDetils);

        return 1;
    }
}
