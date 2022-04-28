package com.example.newcode.config;

import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

	/**
	 * 配置过滤静态资源
	 *
	 * @param web
	 * @throws Exception
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	// 使用我们自己的认证方式

	/**
	 * 授权处理部分
	 *
	 * @param http
	 * @throws Exception
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 配置需要授权检查的页面
		http.authorizeRequests().antMatchers("/comment/add/**", "/follow", "/unfollow", "/followees/**",
				"/followers/**", "/like", "/logout", "/letter/**", "/notice/**", "/discuss/add", "/user/setting",
				"/user/upload", "/user/updatePassword", "/user/profile/**", "/discuss/delete").hasAnyAuthority(
				AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR).antMatchers("/discuss/top", "/discuss/wonderful",
				"/data/**").hasAnyAuthority(AUTHORITY_ADMIN).anyRequest().permitAll().and().csrf().disable();

		// 权限出问题情况
		http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
			// 没有登录的情况
			@Override
			public void commence(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException authException) throws IOException, ServletException {
				// 通过x-requested-with字段来判断是同步还是异步
				String xRequestedWith = request.getHeader("x-requested-with");
				if ("XMLHttpRequest".equals(xRequestedWith)) {
					// 异步请求
					response.setContentType("application/plain;charset=utf-8");
					PrintWriter writer = response.getWriter();
					writer.write(CommunityUtils.getJSONString(403, "你还没有登录哦!"));
				} else {
					// 同步请求，跳转登录的页面
					response.sendRedirect(request.getContextPath() + "/login");
				}
			}
		}).accessDeniedHandler(new AccessDeniedHandler() {
			// 权限不足的情况
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
					AccessDeniedException accessDeniedException) throws IOException, ServletException {

				String xRequestedWith = request.getHeader("x-requested-with");
				if ("XMLHttpRequest".equals(xRequestedWith)) {
					// 异步请求
					response.setContentType("application/plain;charset=utf-8");
					PrintWriter writer = response.getWriter();
					writer.write(CommunityUtils.getJSONString(403, "你没有访问此功能的权限!"));
				} else {
					// 跳转权限不够的页面
					response.sendRedirect(request.getContextPath() + "/denied");
				}
			}
		});

		// Security底层默认会拦截/logout请求,进行退出处理.
		// 覆盖它默认的逻辑,才能执行我们自己的退出代码.
		http.logout().logoutUrl("/recoverSecurityLogout");
	}
}
