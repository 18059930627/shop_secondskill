package com.qf.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author chenzhongjun
 * @Date 2019/12/30
 */

@Controller
public class SystemExceptionHandler implements ErrorController {

    @RequestMapping("/error")
    public String systemHandler(HttpServletResponse response) {
        int status = response.getStatus();
        switch (status) {
            case 404:
                return "404";
            default:
                return "error";
        }
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
