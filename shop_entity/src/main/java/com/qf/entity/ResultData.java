package com.qf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResultData<T> {
    /**
     * 错误码
     */
    private String code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 数据部分
     */
    private T data;

    /**
     * 响应的状态码列表
     */
    public static interface ResultCodeList {
        String OK = "200";
        String ERROR = "500";
    }

}
