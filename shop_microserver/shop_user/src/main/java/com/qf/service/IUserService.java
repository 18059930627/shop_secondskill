package com.qf.service;

import com.qf.entity.User;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
public interface IUserService {
    int register(User user);

    User getUserByUsername(String username);

    int updatePwdByUser(String username, String newPassword);
}
