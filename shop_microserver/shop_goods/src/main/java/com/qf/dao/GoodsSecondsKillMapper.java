package com.qf.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qf.entity.GoodsSecondKill;
import org.apache.ibatis.annotations.Param;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
public interface GoodsSecondsKillMapper extends BaseMapper<GoodsSecondKill> {

    int updateKillSave(@Param("gid") Integer gid, @Param("gnumber") Integer gnumber);
}
