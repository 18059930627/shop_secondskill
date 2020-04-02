package com.qf.feign;

import com.qf.entity.Goods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@FeignClient("MICROSERVER-GOODS")
public interface GoodsFeign {

    /**
     * 添加商品
     *
     * @param goods
     * @return
     */
    @RequestMapping("/goods/insert")
    int insertGoods(@RequestBody Goods goods);

    /**
     * 查询商品
     *
     * @return
     */
    @RequestMapping("/goods/list")
    List<Goods> getGoodsList();

    /**
     * 查询当前场次的秒杀商品信息
     *
     * @param date
     * @return
     */
    @RequestMapping("/goods/queryKillList")
    List<Goods> queryKillList(@RequestBody Date date);

    /**
     * 根据ID查询商品信息
     *
     * @param gid
     * @return
     */
    @RequestMapping("/goods/queryById")
    Goods queryById(@RequestParam("gid") Integer gid);

    /**
     * 修改商品库存
     *
     * @param gid
     * @param gnumber
     * @return
     */
    @RequestMapping("/goods/updateKillSave")
    int updateKillSave(@RequestParam("gid") Integer gid, @RequestParam("gnumber") Integer gnumber);
}
