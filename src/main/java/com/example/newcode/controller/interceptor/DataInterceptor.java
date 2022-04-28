package com.example.newcode.controller.interceptor;

import com.example.newcode.service.DataService;
import com.example.newcode.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

	@Autowired
	DataService dataService;

	@Autowired
	HostHolder hostHolder;

	/**
	 * UV需要不需要登录，DAU需要登录
	 *
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 获取ip地址，存入UV（HyperLogLog），key格式为 uv:yyyyMMdd
		String ip = request.getRemoteHost();
		dataService.recordUV(ip);
		if (hostHolder.getUser() != null) {
			// 获取用户名，存入DAU（BitMap），key格式为 dau:yyyyMMdd
			dataService.recordDAU(hostHolder.getUser().getId());
		}
		return true;
	}
}
