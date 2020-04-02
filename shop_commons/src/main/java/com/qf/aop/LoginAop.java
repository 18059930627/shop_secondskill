package com.qf.aop;

import com.qf.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;

/**
 * @Author chenzhongjun
 * @Date 2019/12/30
 */
@Component
@Aspect//切面类
public class LoginAop {

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("@annotation(IsLogin)")
    public Object isLogin(ProceedingJoinPoint joinPoint) {
        //1、得到cookie
        //得到HttpServletRequest对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        Cookie[] cookies = request.getCookies();
        String loginToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginToken".equals(cookie.getName())) {
                    loginToken = cookie.getValue();
                    break;
                }
            }
        }

        //2、通过cookie中的uuid得到redis中的用户对象
        User user = null;
        if (loginToken != null) {
            user = (User) redisTemplate.opsForValue().get(loginToken);
        }

        //3、判断用户是否为空
        if (user == null) {
            //用户为空，得到方法上的IsLogin注解的参数
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //返回目标方法的签名
            Method method = signature.getMethod();
            //得到目标方法上的IsLogin注解的方法反射对象
            IsLogin isLogin = method.getAnnotation(IsLogin.class);
            //得到值
            boolean login = isLogin.mustLogin();
            if (login) {
                //mustLogin = true 如果为true，则回到登录页面
                //通过request得到请求路径传给登录页面
                //url:  http://localhost:80/a/b   ?  queryString: name=xiaoming&key=value
                String url = "http://localhost" + request.getRequestURI() + "?" + request.getQueryString();
                System.out.println(url);
                try {
                    //如果有中文的话url需要转码
                    url = URLEncoder.encode(url, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return "redirect:http://localhost/sso/toLogin?returnUrl=" + url;
            }
        }

        //4、将用户对象传给Controller
        //用户为空说明：用户未登录且注解的mustLogin = false
        //用户不为空说明：用户已经登录
        //说明Controller还是需要判断用户是否为空
        UserHolder.setUser(user);
       /* //得到目标对象中的方法参数，来设置User对象的属性值
        if(user!=null){
            //访问目标方法的参数
            Object[] args = joinPoint.getArgs();
            if(args!=null&&args.length>0){
                for (int i = 0; i < args.length; i++) {
                    if(args[i].getClass()==User.class){
                        args[i] = user;  //改变方法中参数为User对象的参数
                    }
                }
            }
            try {
                joinPoint.proceed(args);  //将改变后的参数执行目标方法
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }*/

        //调用目标对象的方法
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //清除
        UserHolder.setUser(null);
        return proceed;
    }
}
