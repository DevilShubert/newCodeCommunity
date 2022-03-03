package com.example.newcode.util;

import com.example.newcode.util.entity.MyMailSender;
import com.example.newcode.util.entity.ToEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

@Component
@Slf4j
public class MailClient {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MyMailSender myMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendMail(ToEmail toEmail){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // 利用邮件Helper来辅助生成邮件内容
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(myMailSender.getUsername());
            helper.setTo(toEmail.getTos());
            helper.setSubject(toEmail.getSubject());
            String msg = (String) toEmail.getContent().get("msg");
            helper.setText(msg);
            // 发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            log.error("发送邮件失败: " + e.getMessage());
        }
    }

    public void sendHTMLMail(ToEmail toEmail){
        // 设置thymeleaf页面内容context
        Context context = new Context();
        // 设置收件人名字
        context.setVariable("username", toEmail.getTos());

        // set email
        if (toEmail.getContent().containsKey("email")) {
            String email = (String) toEmail.getContent().get("email");
            context.setVariable("email", email);
        }
        // set url
        if (toEmail.getContent().containsKey("url")) {
            String url = (String) toEmail.getContent().get("url");
            context.setVariable("url", url);
        }

        // 利用模板引擎生成HTML内容的页面（此处是激活用户的页面）
        String process = templateEngine.process("/mail/activation.html", context);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // 利用邮件Helper来辅助生成邮件内容
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 设置发件人
            helper.setFrom(myMailSender.getUsername());

            // 设置收件人email
            helper.setTo((String) toEmail.getContent().get("email"));
            // 设置主题
            helper.setSubject(toEmail.getSubject());
            // 设置内容为templateEngine生成的HTML页面，设置TRUE会自动识别是否是HTML页面
            helper.setText(process,true);
            // 发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
