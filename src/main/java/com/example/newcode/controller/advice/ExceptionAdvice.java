package com.example.newcode.controller.advice;

import com.example.newcode.util.CommunityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class ExceptionAdvice {

	/**
	 * 统一处理异常，实现思路：
	 * 1、如果是ajax异常则将对应处理异常的json格式以流的形式插入到response
	 * 2、如果是页面跳转异常则携带错误信息重定向到500页面（404springboot会自动跳转到404页面）
	 */
	@ExceptionHandler(Exception.class)
	public void handleException(HttpServletRequest request, HttpServletResponse response, Exception e)
			throws IOException {
		//  注意这里的日志输出分两部分，因为在AopLog中会切面加入记录错误日志的代码（记录时那个用户用户的访问而导致了错误）
		//  其次是在这里看打印具体的错误信息
		log.error("服务器发生异常: " + e.getMessage());
		for (StackTraceElement element : e.getStackTrace()) {
			log.error(element.toString());
		}

		String xRequestedWith = request.getHeader("x-requested-with");
		if ("XMLHttpRequest".equals(xRequestedWith)) {
			// ajax请求异常
			response.setContentType("application/plain;charset=utf-8");
			PrintWriter writer = response.getWriter();
			writer.write(CommunityUtils.getJSONString(1, "服务器异常!"));
		} else {
			// 页面跳转发生异常
			response.sendRedirect(request.getContextPath() + "/error");
		}
	}

	@RequestMapping(path = "/error", method = RequestMethod.GET)
	public String getErrorPage() {
		log.info("test");
		return "/error/500";
	}
}
