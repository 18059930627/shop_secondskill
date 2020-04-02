package com.qf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@Data
@Accessors(chain = true)
@TableName("t_goods_seconds_kill")
public class GoodsSecondKill extends BaseEntity {
    private Integer gid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private BigDecimal killPrice;

    private Integer killSave;
}
