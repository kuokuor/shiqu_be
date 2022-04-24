package com.kuokuor.shiqu.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 发送邮件类
 *
 * @Author: GreatBiscuit
 * @Date: 2022/3/16 13:16
 */
@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender mailSender;

    public String sendMail(String to, String subject, String content) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom("hyxk_station@foxmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            return "发送邮件出错!";
        }

        return null;

    }

}
