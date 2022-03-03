package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.LoginTicketDao;
import com.example.newcode.dao.UserDao;
import com.example.newcode.entity.LoginTicket;
import com.example.newcode.entity.User;
import com.example.newcode.service.UserLoginService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserLoginServiceImpl  extends ServiceImpl<LoginTicketDao, LoginTicket> implements UserLoginService   {

    @Autowired
    LoginTicketDao loginTicketDao;

    @Autowired
    UserService userService;

    @Autowired
    UserLoginService userLoginService;

    @Override
    public Boolean insertLoginTicket(LoginTicket loginTicket) {
        return loginTicketDao.insert(loginTicket) > 0;
    }

    @Override
    public LoginTicket selectByTicket(String ticket) {
        QueryWrapper<LoginTicket> wrapper = new QueryWrapper<>();
        wrapper.eq("ticket", ticket);
        return loginTicketDao.selectOne(wrapper);
    }

    @Override
    public Boolean updateStatus(String ticket, int status) {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setStatus(status);
        UpdateWrapper<LoginTicket> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("ticket",ticket);
        // update with remaining old data
        return loginTicketDao.update(loginTicket, updateWrapper) > 0;
    }

    @Override
    public Map<String, Object> doLogin(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        List<User> users = userService.selectByName(username);
        if (users == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }
        User user = new User();
        if (users.size() == 1)  user = users.get(0);

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtils.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtils.getRandomUUID());
        loginTicket.setStatus(0);
        Date date = new Date(System.currentTimeMillis() + expiredSeconds * 1000);
        loginTicket.setExpired(date);
        userLoginService.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public Boolean logout(String ticket) {
        return userLoginService.updateStatus(ticket, 1);
    }
}
