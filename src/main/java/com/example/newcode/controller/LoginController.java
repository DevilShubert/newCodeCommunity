package com.example.newcode.controller;

import com.example.newcode.service.UserLoginService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.RedisUtils;
import com.example.newcode.util.constant.CommunityConstant;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class LoginController implements CommunityConstant {

	@Autowired
	UserLoginService userLoginService;

	@Autowired
	DefaultKaptcha defaultKaptcha;

	@Autowired
	RedisUtils redisUtils;

	@Value("${community.path.domain}")
	private String contextPath;

	/**
	 * 返回到登录的前端页面
	 *
	 * @return
	 */
	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String getLoginPage() {
		return "/site/login";
	}

	/**
	 * 获得登录验证码
	 *
	 * @param response
	 */
	@RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
	public void getDefaultKaptcha(HttpServletResponse response) {
		// 生成验证码
		String kaptchaCode = defaultKaptcha.createText();
		log.info(kaptchaCode);
		BufferedImage image = defaultKaptcha.createImage(kaptchaCode);

		// 当前线程验证码归属
		String kaptchaOwner = CommunityUtils.getRandomUUID();

		// 为当前线程生成cookie，并存入cookie
		Cookie kaptchaOwnerCookie = new Cookie("kaptchaOwner", kaptchaOwner);
		int expired = 3600 * 24;
		kaptchaOwnerCookie.setMaxAge(expired);
		kaptchaOwnerCookie.setPath(contextPath);
		response.addCookie(kaptchaOwnerCookie);

		// text存入redis
		String redisKey = RedisUtils.getKaptchaKey(kaptchaOwner);
		redisUtils.setWithExpire(redisKey, kaptchaCode, (long) expired, TimeUnit.SECONDS);

		// 将突图片输出给浏览器
		response.setContentType("image/png");
		try {
			// 利用流输出给图片
			ServletOutputStream outputStream = response.getOutputStream();
			ImageIO.write(image, "png", outputStream);
		} catch (IOException e) {
			log.error("响应验证码失败:" + e.getMessage());
		}

	}

	/**
	 * 执行登录认证操作（成功则存ticket）
	 *
	 * @param username
	 * @param password
	 * @param code
	 * @param rememberme
	 * @param model
	 * @param response
	 * @param kaptchaOwner
	 * @return
	 */
	@RequestMapping(value = "/doLogin", method = RequestMethod.POST)
	public String doLogin(String username, String password, String code, boolean rememberme, Model model,
			HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
		// 1: check kaptcha（作为）
		String kaptcha = null;
		if (StringUtils.isNoneBlank(kaptchaOwner)) {
			String redisKey = RedisUtils.getKaptchaKey(kaptchaOwner);
			kaptcha = (String) redisUtils.get(redisKey);
		}

		if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
			model.addAttribute("codeMsg", "验证码不正确!");
			return "/site/login";
		}

		// 2: check rememberMe
		long expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

		// 3: chechk password and insert login_ticket or not
		Map<String, Object> map = userLoginService.doLogin(username, password, expiredSeconds);
		// user corrected
		if (map.get("ticket") != null) {
			// 4: store the ticket into cookie
			Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
			// set cookie useful location
			cookie.setPath(contextPath);
			response.addCookie(cookie);
			return "redirect:/indexPage";
		} else {
			// 5: not corrected
			model.addAttribute("usernameMsg", map.get("usernameMsg"));
			model.addAttribute("passwordMsg", map.get("passwordMsg"));
			return "/site/login";
		}
	}

	/**
	 * 执行退出/注销操作
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
		Cookie[] cookies = request.getCookies();
		// cookies中也有多个cookie，例如rememberMe的cookie，kaptchaOwner验证码的cookie
		for (Cookie cookie : cookies) {
			if ("ticket".equals(cookie.getName())) {
				// 这里不仅让存储在数据库的凭证失效，还将将ticket的cookie清除
				userLoginService.logout(cookie.getValue());
				cookie.setMaxAge(0);
				response.addCookie(cookie); // 通过设置存活时间来，删除当前的cookie
				SecurityContextHolder.clearContext();
			}
		}
		return "redirect:/indexPage";

	}
}
