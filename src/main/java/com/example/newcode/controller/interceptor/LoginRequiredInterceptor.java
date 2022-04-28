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
	// LoginRequiredInterceptor拦截器在使用Spring-Security之后就不再被使用了
	@Autowired
	HostHolder hostHolder;

	@Value("${server.servlet.context-path}")
	String context;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
			// 获取到对应访问API的方法
			Method method = ((HandlerMethod) handler).getMethod();
			// 获取到是否有这个注解
			LoginRequired annotation = method.getAnnotation(LoginRequired.class);
			if (annotation != null && hostHolder.getUser() == null) {
				response.sendRedirect(context + "/indexPage");
				return false;
			}
		}
		return true;
	}
}
