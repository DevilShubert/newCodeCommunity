package com.example.newcode.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.LoginTicket;

import java.util.Map;

public interface UserLoginService extends IService<LoginTicket> {
    Boolean insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    Boolean updateStatus(String ticket, int status);

    Map<String, Object> doLogin(String username, String password, long expiredSeconds);

    Boolean logout(String ticket);
}
