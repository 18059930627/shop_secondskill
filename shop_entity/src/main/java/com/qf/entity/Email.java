package com.qf.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Data
@Accessors(chain = true)
public class Email implements Serializable {

    /**
     * 邮件标题
     */
    private String subject;

    /**
     * 邮件接收方
     */
    private String to;


    /*
     *发送内容
     */
    private String content;
}
