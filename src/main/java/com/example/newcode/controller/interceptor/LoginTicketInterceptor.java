package com.example.newcode.controller.interceptor;

import com.example.newcode.entity.LoginTicket;
import com.example.newcode.entity.User;
import com.example.newcode.service.UserLoginService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserLoginService userLoginService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CommunityUtils.getCookieValue(request, "ticket");
        // get ticket cookie value
        if (ticket != null) {
            LoginTicket selectTicket = userLoginService.selectByTicket(ticket);
            // 3 conditions
            if (selectTicket != null && selectTicket.getStatus() == 0 && selectTicket.getExpired().after(new Date())) {
                User user = userService.selectById(selectTicket.getUserId());
                // store to ThreadLocal
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
