package com.example.newcode.controller.interceptor;

import com.example.newcode.entity.User;
import com.example.newcode.service.MessageService;
import com.example.newcode.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private MessageService messageService;

	// MessageInterceptor拦截器的作用时间：在controller处理完之后，modelAndView返回给前端页面渲染时
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		//必须是用户登陆了且正常的页面跳转
		if (user != null && modelAndView != null) {
			int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
			int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
			modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
		}
	}
}
