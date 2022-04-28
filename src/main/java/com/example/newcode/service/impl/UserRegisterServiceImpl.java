package com.example.newcode.service.impl;

import com.example.newcode.entity.User;
import com.example.newcode.service.UserRegisterService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.MailClient;
import com.example.newcode.util.constant.CommunityConstant;
import com.example.newcode.util.entity.ToEmail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserRegisterServiceImpl implements UserRegisterService, CommunityConstant {

	@Autowired
	UserService userService;

	@Value("${community.path.domain}")
	private String domain;

	@Autowired
	private MailClient mailClient;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	@Override
	public Map<String, Object> doRegister(User user) {
		HashMap<String, Object> hashMap = new HashMap<>();

		if (user == null) {
			throw new IllegalArgumentException("参数不能为空");
		}

		if (StringUtils.isBlank(user.getUsername())) {
			hashMap.put("usernameMsg", "账号不能为空!");
			return hashMap;
		}
		if (StringUtils.isBlank(user.getPassword())) {
			hashMap.put("passwordMsg", "密码不能为空!");
			return hashMap;
		}
		if (StringUtils.isBlank(user.getEmail())) {
			hashMap.put("emailMsg", "邮箱不能为空!");
			return hashMap;
		}

		List<User> selectByName = userService.selectByName(user.getUsername());
		if (selectByName.size() != 0) {
			hashMap.put("usernameMsg", "该账号已存在!");
			return hashMap;
		}

		List<User> selectByEmail = userService.selectByEmail(user.getEmail());
		if (selectByEmail.size() != 0) {
			hashMap.put("emailMsg", "该邮箱已被注册!");
			return hashMap;
		}

		// generate salt by sub UUID
		String salt = CommunityUtils.getRandomUUID().substring(0, 5);
		// init user
		user.setSalt(salt);
		// generate md5's pwd by (pwd + salt)
		user.setPassword(CommunityUtils.md5(user.getPassword() + user.getSalt()));
		user.setType(0);
		user.setStatus(0);
		// RandomActivationCode
		user.setActivationCode(CommunityUtils.getRandomUUID());
		user.setCreateTime(new Date());
		// random picture
		user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
		userService.insertUser(user);

		// send active email
		HashMap<String, Object> contentMap = new HashMap<>();
		contentMap.put("email", user.getEmail());
		// mybatis-plus reload the userID after inserting action
		// http://localhost:85/community
		String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
		contentMap.put("url", url);
		ToEmail toEmail = new ToEmail(user.getUsername(), "激活账号", contentMap);
		mailClient.sendHTMLMail(toEmail);
		return null;
	}

	@Override
	public int activation(int userId, String code) {
		User user = userService.selectById(userId);
		if (user.getStatus() == 1) {
			return ACTIVATION_REPEAT;
		} else if (user.getActivationCode().equals(code)) {
			// update status by userId
			userService.updateStatus(userId, 1, user);
			return ACTIVATION_SUCCESS;
		} else {
			return ACTIVATION_FAILURE;
		}
	}
}
