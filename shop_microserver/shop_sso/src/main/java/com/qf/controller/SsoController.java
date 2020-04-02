package com.qf.controller;

import com.qf.entity.ResultData;
import com.qf.entity.User;
import com.qf.feign.UserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Controller
@RequestMapping("/sso")
public class SsoController {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录页面
     *
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    /**
     * 注册页面
     *
     * @return
     */
    @RequestMapping("/toRegister")
    public String toRegister() {
        return "register";
    }


    @RequestMapping("register")
    public String register(User user, ModelMap map) {
        int result = userFeign.register(user);
        if (result > 0) {
            //注册成功
            return "login";
        } else if (result == -1) {
            //用户名存在
            map.put("msg", "用户名已经存在");
        } else {
            map.put("msg", "注册失败");
        }
        return "register";
    }

    /**
     * 用户登录认证
     *
     * @param username
     * @param password
     * @param rememberMe
     * @param map
     * @param response
     * @param returnUrl
     * @return
     */
    @RequestMapping("login")
    public String login(String username, String password, String rememberMe, ModelMap map, HttpServletResponse response, String returnUrl) { //on
        User user = userFeign.getUserByUsername(username);
        if (user == null) {
            //登录失败
            map.put("msg", "用户名或密码错误");
        } else {
            //判断密码是否相同
            if (user.getPassword().equals(password)) {
                //登录成功
                //是否有记住密码
                if ("on".equals(rememberMe)) {
                    String uuid = UUID.randomUUID().toString();
                    //将用户对象保存到redis中,并且设置有限期为7天
                    redisTemplate.opsForValue().set(uuid, user, 7, TimeUnit.DAYS);
                    //将uuid保存到cookie，有效期为7天
                    Cookie cookie = new Cookie("loginToken", uuid);
                    cookie.setMaxAge(60 * 60 * 24 * 7);
                    //cookie跨域问题
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
                //首页
                if (returnUrl == null || "".equals(returnUrl)) {
                    return "redirect:http://localhost/";
                } else {

                    //跳到之前的页面
                    return "redirect:" + returnUrl;
                }

            } else {
                //登录失败，密码错误
                map.put("msg", "用户名或密码错误");
            }
        }
        return "login";
    }

    /**
     * 判断用户是否登录
     *
     * @param loginToken
     * @return
     */
    @RequestMapping("isLogin")
    @ResponseBody
    public ResultData<User> isLogin(@CookieValue(name = "loginToken", required = false) String loginToken) {
        if (!"".equals(loginToken) && loginToken != null) {
            //用户登录
            //通过loginToken通过redis得到用户对象
            User user = (User) redisTemplate.opsForValue().get(loginToken);
            if (user != null) {
                return new ResultData<User>().setCode(ResultData.ResultCodeList.OK).setData(user);
            }
        }
        //用户未登录
        return new ResultData<User>().setCode(ResultData.ResultCodeList.ERROR).setMsg("用户未登录");
    }

    /**
     * 用户注销登录
     *
     * @param loginToken
     * @param response
     * @return
     */
    @RequestMapping("logout")
    public String logout(@CookieValue(name = "loginToken", required = false) String loginToken, HttpServletResponse response) {
        //清除redis
        redisTemplate.delete(loginToken);
        //清除cookie
        Cookie cookie = new Cookie("loginToken", "1");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        //返回登录页面
        return "redirect:http://localhost:/sso/toLogin";
    }
}
