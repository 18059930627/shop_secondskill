package com.qf.controller;

import com.netflix.discovery.converters.Auto;
import com.qf.entity.Email;
import com.qf.entity.ResultData;
import com.qf.entity.User;
import com.qf.feign.UserFeign;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Controller
@RequestMapping("/forget")
public class ForgetController {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 到忘记密码页面
     *
     * @return
     */
    @RequestMapping("toForgetPwd")
    public String toForgetPwd() {
        return "forget_password";
    }

    /**
     * 忘记密码发送邮箱
     *
     * @param username
     * @return
     */
    @RequestMapping("sendMail")
    @ResponseBody
    public ResultData<Map<String, String>> sendMail(String username) {
        User user = userFeign.getUserByUsername(username);
        if (user != null) {
            String strEmail = user.getEmail();
            //唯一标识
            String uuid = UUID.randomUUID().toString();
            //将uuid存进redis中,并设置有效时间为5min
            redisTemplate.opsForValue().set(uuid, user.getUsername(), 5, TimeUnit.MINUTES);
            //用户点击的修改密码链接
            String url = "http://localhost/forget/toUpdatePwd?token=" + uuid;
            //邮件对象
            Email email = new Email()
                    .setTo(strEmail)
                    .setSubject("忘记密码")
                    .setContent("找回密码，请点击<a target='_blank' href='" + url + "'>这里</a>");
            //通过mq发送邮件
            rabbitTemplate.convertAndSend("mail_exchange", "", email);

            //在前台页面显示用户的邮箱以及邮箱的访问地址
            Map<String, String> map = new HashMap<>();
            String mStr = strEmail.substring(3, strEmail.lastIndexOf("@"));
            String showEmail = strEmail.replace(mStr, "********");
            map.put("showEmail", showEmail);
            //跳转的地址
            String toEmail = "mail." + strEmail.substring(strEmail.lastIndexOf("@") + 1);
            map.put("toEmail", toEmail);
            return new ResultData<Map<String, String>>().setCode(ResultData.ResultCodeList.OK).setData(map);
        }
        return new ResultData<Map<String, String>>().setCode(ResultData.ResultCodeList.ERROR).setMsg("不存在该用户");
    }

    /**
     * 跳转到修改密码页面
     *
     * @return
     */
    @RequestMapping("toUpdatePwd")
    public String toUpdatePwd() {
        return "update_password";
    }

    /**
     * 修改密码
     *
     * @param token
     * @param newPassword
     * @return
     */
    @RequestMapping("updatePassword")
    public String updatePassword(String token, String newPassword) {
        //通过token去redis中判断
        String username = (String) redisTemplate.opsForValue().get(token);
        if (username != null && !"".equals(username)) {
            //修改密码
            int result = userFeign.updatePwdByUser(username, newPassword);
            if (result > 0) {
                //成功,删除redis中的数据
                redisTemplate.delete(token);
                //跳转到登录页面
                return "login";
            }
        }
        return "update_error";
    }
}
