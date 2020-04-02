package com.qf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@Controller
public class PageController {

    @RequestMapping("showPage/{page}")
    public String toPage(@PathVariable("page") String page) {
        return page;
    }
}
