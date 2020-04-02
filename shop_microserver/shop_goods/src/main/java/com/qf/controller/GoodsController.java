package com.qf.controller;

import com.netflix.discovery.converters.Auto;
import com.qf.entity.Goods;
import com.qf.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @RequestMapping("/insert")
    public int insertGoods(@RequestBody Goods goods) {
        return goodsService.insertGoods(goods);
    }

    @RequestMapping("/list")
    public List<Goods> getGoodsList() {
        return goodsService.getGoodsList();
    }


    /**
     * 查询当前场次的秒杀商品信息
     *
     * @param date
     * @return
     */
    @RequestMapping("/queryKillList")
    public List<Goods> queryKillList(@RequestBody Date date) {
        return goodsService.queryKillList(date);
    }

    /**
     * 根据ID查询商品信息
     *
     * @param gid
     * @return
     */
    @RequestMapping("/queryById")
    public Goods queryById(@RequestParam("gid") Integer gid) {
        return goodsService.queryById(gid);
    }

    /**
     * 修改商品库存
     *
     * @param gid
     * @param gnumber
     * @return
     */
    @RequestMapping("/updateKillSave")
    public int updateKillSave(@RequestParam("gid") Integer gid, @RequestParam("gnumber") Integer gnumber) {
        return goodsService.updateKillSave(gid, gnumber);
    }
}
