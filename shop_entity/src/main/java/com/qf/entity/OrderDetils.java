package com.qf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@TableName("t_order_details")
public class OrderDetils extends BaseEntity {

    private Integer orderId;
    private Integer goodsId;
    private String title;
    private BigDecimal price;
    private Integer number;
    private String fmurl;
    private BigDecimal detailsPrice;
}
