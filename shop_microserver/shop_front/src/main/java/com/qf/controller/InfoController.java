package com.qf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author chenzhongjun
 * @Date 2020/1/1
 */
@Controller
@RequestMapping("/info")
public class InfoController {

    @RequestMapping("error")
    public String error() {
        return "error";
    }
}
