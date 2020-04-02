package com.qf.feign;

import com.qf.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@FeignClient("MICROSERVER-USER")
public interface UserFeign {

    @RequestMapping("/user/register")
    int register(@RequestBody User user);

    @RequestMapping("/user/getUserByUsername")
    User getUserByUsername(@RequestParam("username") String username);

    @RequestMapping("/user/updatePwdByUser")
    int updatePwdByUser(@RequestParam("username") String username,
                        @RequestParam("newPassword") String newPassword);
}
