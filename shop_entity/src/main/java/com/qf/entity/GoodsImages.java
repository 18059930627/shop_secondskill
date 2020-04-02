package com.qf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@Data
@Accessors(chain = true)
@TableName("t_goods_images")
public class GoodsImages extends BaseEntity {
    private String url;

    private String description;

    private Integer iscover;

    private Integer goodsId;
}
