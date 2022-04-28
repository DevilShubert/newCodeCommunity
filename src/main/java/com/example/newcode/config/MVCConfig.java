package com.example.newcode.config;

import com.example.newcode.controller.interceptor.DataInterceptor;
import com.example.newcode.controller.interceptor.LoginTicketInterceptor;
import com.example.newcode.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class MVCConfig implements WebMvcConfigurer {
	@Autowired
	LoginTicketInterceptor loginTicketInterceptor;

	@Autowired
	MessageInterceptor messageInterceptor;

	@Autowired
	DataInterceptor dataInterceptor;

	//	@Autowired
	//	LoginRequiredInterceptor loginRequiredInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 不对以下的这些个静态资源进行拦截
		registry.addInterceptor(loginTicketInterceptor).excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png",
				"/**/*.jpg", "/**/*.jpeg");

		//		registry.addInterceptor(loginRequiredInterceptor).excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png",
		//				"/**/*.jpg", "/**/*.jpeg");

		registry.addInterceptor(messageInterceptor).excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png",
				"/**/*.jpg", "/**/*.jpeg");

		registry.addInterceptor(dataInterceptor).excludePathPatterns().excludePathPatterns("/**/*.css", "/**/*.js",
				"/**/*.png", "/**/*.jpg", "/**/*.jpeg");
	}
}
