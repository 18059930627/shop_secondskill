package com.qf.aop;

import com.qf.entity.User;

/**
 * @Author chenzhongjun
 * @Date 2019/12/30
 */
public class UserHolder {
    private static ThreadLocal<User> user = new ThreadLocal<>();

    /**
     * 获取用户对象
     *
     * @return
     */
    public static User getUser() {
        return user.get();
    }

    /**
     * 判断用户是否已经登录
     *
     * @return
     */
    public static boolean isLogin() {
        return user.get() != null;
    }

    /**
     * 设置用户
     *
     * @param user
     */
    public static void setUser(User user) {
        UserHolder.user.set(user);
    }
}
