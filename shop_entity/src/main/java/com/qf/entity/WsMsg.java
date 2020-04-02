package com.qf.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Data
@Accessors(chain = true)
public class WsMsg<T> implements Serializable {

    /**
     * 发送方
     */
    private Integer fromId;

    /**
     * 接受方
     */
    private Integer toId;

    /**
     * 消息的类型
     * 1 - 初始化信息
     * 2 - 心跳信息
     * 3 - 秒杀信息
     */
    private Integer type;

    /**
     * 消息的内容
     */
    private T data;
}
