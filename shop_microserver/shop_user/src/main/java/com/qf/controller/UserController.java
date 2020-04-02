package com.qf.controller;

import com.netflix.discovery.converters.Auto;
import com.qf.entity.User;
import com.qf.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    @RequestMapping("/register")
    public int register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 通过用户名查询用户对象
     *
     * @param username
     * @return
     */
    @RequestMapping("getUserByUsername")
    public User getUserByUsername(@RequestParam("username") String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * 用户修改密码
     *
     * @param username
     * @param newPassword
     * @return
     */
    @RequestMapping("updatePwdByUser")
    public int updatePwdByUser(@RequestParam("username") String username,
                               @RequestParam("newPassword") String newPassword) {
        return userService.updatePwdByUser(username, newPassword);
    }

}
