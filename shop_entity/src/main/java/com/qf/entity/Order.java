package com.qf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author chenzhongjun
 * @Date 2020/1/2
 */
@Data
@Accessors(chain = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private String orderId;
    private Integer uid;
    private String person;
    private String address;
    private String phone;
    private String code;
    private BigDecimal allPrice;

    @TableField(exist = false)
    private List<OrderDetils> orderDetils;
}
