package com.qf.listener;

import com.qf.entity.Email;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * @Author chenzhongjun
 * @Date 2019/12/29
 */
@Component
public class MyRabbitMQListener {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件
     *
     * @param email
     * @throws MessagingException
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = "mail_exchange", type = "fanout"),
            value = @Queue(name = "mail_queue")))
    public void handlerMsg(Email email) throws MessagingException {
        //创建一封邮件
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        //邮箱快捷对象
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false);
        //设置标题
        messageHelper.setSubject(email.getSubject());

        //设置发送方
        messageHelper.setFrom(from);
        //接收方法
        messageHelper.setTo(email.getTo());
        //发送的内容
        messageHelper.setText(email.getContent(), true);
        //发送的时间
        messageHelper.setSentDate(new Date());
        //发送邮件
        javaMailSender.send(mimeMessage);
    }
}
