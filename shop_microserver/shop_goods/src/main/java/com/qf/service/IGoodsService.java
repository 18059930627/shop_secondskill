package com.qf.service;

import com.qf.entity.Goods;

import java.util.Date;
import java.util.List;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
public interface IGoodsService {
    int insertGoods(Goods goods);

    List<Goods> getGoodsList();

    List<Goods> queryKillList(Date date);

    int updateKillSave(Integer gid, Integer gnumber);

    Goods queryById(Integer gid);
}
