package com.example.newcode.controller.interceptor;

import com.example.newcode.entity.LoginTicket;
import com.example.newcode.entity.User;
import com.example.newcode.service.UserLoginService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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

	// 调用时间：Controller方法处理之前
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// 通过cookie得到ticket
		String ticket = CommunityUtils.getCookieValue(request, "ticket");
		// 如果cookie中的ticket存在则肯定说明当前浏览器之前是登录的了的
		if (ticket != null) {
			LoginTicket selectTicket = userLoginService.selectByTicket(ticket);
			// 3 conditions，说明凭证有效
			if (selectTicket != null && selectTicket.getStatus() == 0 && selectTicket.getExpired().after(new Date())) {
				User user = userService.selectById(selectTicket.getUserId());
				// 通过hostHolder来保护用户的"状态"，是整个项目的关键
				hostHolder.setUser(user);
				// 构建用户认证的结果,并存入SecurityContext,以便于Security框架获取进行授权.
				Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
						userService.getAuthorities(user.getId()));
				SecurityContextImpl context = new SecurityContextImpl(authentication);
				SecurityContextHolder.setContext(context);
			}
		}
		return true;
	}

	// 后置拦截器，作用是在传递给前端模板时会有带有参数：loginUser - user
	// 调用时间：Controller方法处理完之后，DispatcherServlet进行视图的渲染之前
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		if (user != null && modelAndView != null) {
			modelAndView.addObject("loginUser", user);
		}
	}

	// 调用时间：DispatcherServlet进行视图的渲染之后
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		hostHolder.clear();
	}
}
