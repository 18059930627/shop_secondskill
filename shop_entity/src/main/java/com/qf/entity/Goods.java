package com.qf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */

@Data
@Accessors(chain = true)
@TableName("t_goods")
public class Goods extends BaseEntity {
    private String title;

    private BigDecimal price;

    private Integer storage;

    private String description;

    /*
     * 1 - 普通商品
     * 2 - 秒杀商品
     */
    private Integer type;

    @TableField(exist = false)
    private String fmUrl;

    @TableField(exist = false)
    private List<String> otherUrls = new ArrayList<>();

    @TableField(exist = false)
    private GoodsSecondKill goodsSecondKill;

    public void addOtherUrl(String url) {
        otherUrls.add(url);
    }
}
