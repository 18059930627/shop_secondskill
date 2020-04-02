package com.qf.controller;

import com.qf.entity.ResultData;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author chenzhongjun
 * @Date 2019/12/30
 */
@ControllerAdvice
public class ExceptionController {

    /**
     * 统一异常处理
     *
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exceptionHandler(Exception e, HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        System.out.println(e.getMessage());
        if ("XMLHttpRequest".equals(header)) {
            //ajax请求发生错误,返回请求失败
            return new ResultData<String>()
                    .setCode(ResultData.ResultCodeList.ERROR)
                    .setMsg("系统繁忙，请稍后重试");
        }
        //普通请求，返回页面
        return new ModelAndView("error");
    }
}
