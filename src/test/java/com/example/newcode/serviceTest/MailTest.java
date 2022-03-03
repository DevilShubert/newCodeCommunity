package com.example.newcode.serviceTest;

import com.example.newcode.util.MailClient;
import com.example.newcode.util.entity.MyMailSender;
import com.example.newcode.util.entity.ToEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Test
    public void mailTest(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg","邮件正文");
        ToEmail toEmail = new ToEmail("2302265572@qq.com", "测试HTML主题", map);
        mailClient.sendMail(toEmail);
    }

    @Test
    public void mailHTMLTest(){
        HashMap<String, Object> contentMap = new HashMap<>();
        contentMap.put("email", "2302265572@qq.com");
        // mybatis-plus reload the userID after inserting action
        String url = "http://localhost:8080/community/activation/101/code";
        contentMap.put("url", url);
        ToEmail toEmail = new ToEmail("2302265572@qq.com", "激活账号", contentMap);
        mailClient.sendHTMLMail(toEmail);
    }

}
