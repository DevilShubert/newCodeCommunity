package com.example.newcode.serviceTest;

import com.example.newcode.entity.LoginTicket;
import com.example.newcode.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
@Slf4j
public class UserLoginServiceTest {

    @Autowired
    UserLoginService userLoginService;



    @Test
    public void testInsert(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(123123);
        loginTicket.setTicket("123456");
        loginTicket.setStatus(1);
        Date date = new Date();
        loginTicket.setExpired(date);

        userLoginService.insertLoginTicket(loginTicket);
    }

    @Test
    public void testUpate(){
        userLoginService.updateStatus("123456",0);
    }

    @Test
    public void testSelect(){
        LoginTicket ticket = userLoginService.selectByTicket("123456");
        log.info(ticket.toString());
    }

    @Test
    public void testLoginService(){
        userLoginService.doLogin("LZR", "LIUzheran..123", 30);
    }
}
