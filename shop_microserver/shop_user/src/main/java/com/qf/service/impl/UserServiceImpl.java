package com.qf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qf.dao.UserMapper;
import com.qf.entity.User;
import com.qf.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册用户
     *
     * @param user
     * @return
     */
    @Override
    @Transactional
    public int register(User user) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", user.getUsername());
        User user1 = userMapper.selectOne(queryWrapper);
        if (user1 == null) {
            return userMapper.insert(user);
        } else {
            return -1;
        }


    }

    /**
     * 通过用户名查询用户对象
     *
     * @param username
     * @return
     */
    @Override
    public User getUserByUsername(String username) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        return user;
    }

    /**
     * 用户修改面
     *
     * @param username
     * @param newPassword
     * @return
     */
    @Override
    @Transactional
    public int updatePwdByUser(String username, String newPassword) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        user.setPassword(newPassword);
        //修改密码
        return userMapper.updateById(user);
    }
}
