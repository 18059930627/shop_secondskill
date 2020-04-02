package com.qf.aop;


import java.lang.annotation.*;

/**
 * @Author chenzhongjun
 * @Date 2019/12/30
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IsLogin {

    boolean mustLogin() default false;
}
