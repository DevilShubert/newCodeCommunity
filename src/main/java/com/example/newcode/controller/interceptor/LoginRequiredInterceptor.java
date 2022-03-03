package com.example.newcode.controller.interceptor;

import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;


@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;

    @Value("${server.servlet.context-path}")
    String context;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            if (annotation != null && hostHolder.getUser() == null){
                response.sendRedirect(context + "/indexPage");
                return  false;
            }
        }
        return true;
    }
}
