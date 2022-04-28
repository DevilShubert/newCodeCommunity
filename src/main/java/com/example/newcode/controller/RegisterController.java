package com.example.newcode.controller;

import com.example.newcode.entity.User;
import com.example.newcode.service.UserRegisterService;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class RegisterController implements CommunityConstant {

	@Autowired
	UserRegisterService userRegisterService;

	/**
	 * 返回前端注册的页面
	 *
	 * @return
	 */
	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String registerPage() {
		return "site/register";
	}

	/**
	 * 执行用户注册操作
	 *
	 * @param model
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String doRegister(Model model, User user) {
		Map<String, Object> resMap = userRegisterService.doRegister(user);
		if (resMap == null) {
			model.addAttribute("msg", "我们已经向您的邮箱发送了一封激活邮件, 请尽快激活以完成注册!");
			model.addAttribute("target", "/indexPage");
			return "site/operate-result";
		} else {
			model.addAttribute("usernameMsg", resMap.get("usernameMsg"));
			model.addAttribute("passwordMsg", resMap.get("passwordMsg"));  // useless
			model.addAttribute("emailMsg", resMap.get("emailMsg"));
			return "site/register";
		}
	}

	/**
	 * 通过邮箱中的激活码激活账户
	 *
	 * @param model
	 * @param userId
	 * @param code
	 * @return
	 */
	// activeLink: http://localhost:8080/community/activation/101/XXXcode
	@RequestMapping(value = "/activation/{userId}/{code}", method = RequestMethod.GET)
	public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
		int activation = userRegisterService.activation(userId, code);
		if (activation == ACTIVATION_SUCCESS) {
			model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
			model.addAttribute("target", "/login");
		} else if (activation == ACTIVATION_REPEAT) {
			model.addAttribute("msg", "无效操作,该账号已经激活过了!");
			model.addAttribute("target", "/indexPage");
		} else {
			model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
			model.addAttribute("target", "/indexPage");
		}
		return "/site/operate-result";
	}
}
