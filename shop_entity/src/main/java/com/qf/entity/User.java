package com.qf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Data
@Accessors(chain = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String phone;

    private String email;
}
