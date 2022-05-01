package com.example.newcode.controller;

import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.User;
import com.example.newcode.service.FollowService;
import com.example.newcode.service.LikeService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController implements CommunityConstant {

	@Autowired
	HostHolder hostHolder;

	@Autowired
	UserService userService;

	@Autowired
	LikeService likeService;

	@Autowired
	FollowService followService;

	@Value("${community.path.domain}")
	String domainPath;

	@Value("${community.path.upload}")
	String uploadPath;

	@Value("${server.servlet.context-path}")
	String context;

	@Value("${qiniu.key.access}")
	String accessKey;

	@Value("${qiniu.key.secret}")
	String secretKey;

	@Value("${qiniu.bucket.header.name}") // header的空间名
	private String headerBucketName;

	@Value("${qiniu.bucket.header.url}") // header空间的域名
	private String headerBucketUrl;

	/**
	 * 返回前端的设置页面
	 *
	 * @return
	 */
	@RequestMapping("/setting")
	@LoginRequired
	public String getSettingPage(Model model) {
		// 上传文件名称
		String fileName = CommunityUtils.getRandomUUID();
		// 设置期望从七牛云服务端获得的响应信息
		StringMap policy = new StringMap();
		// 希望七牛云上传的结果是一个异步响应
		policy.put("returnBody", CommunityUtils.getJSONString(0));
		// 生成上传凭证
		Auth auth = Auth.create(accessKey, secretKey);
		String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

		model.addAttribute("uploadToken", uploadToken);
		model.addAttribute("fileName", fileName);

		return "/site/setting";
	}

	/**
	 * 使用七牛云后更新header_url字段的值
	 * @param fileName
	 * @return
	 */
	@RequestMapping(path = "/header/url/{fileName}", method = RequestMethod.POST)
	@ResponseBody
	public String updateHeaderUrl(@PathVariable("fileName") String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return CommunityUtils.getJSONString(1, "文件名不能为空!");
		}

		String url = headerBucketUrl + "/" + fileName;
		userService.updateHeader(hostHolder.getUser().getId(), url, hostHolder.getUser());
		return CommunityUtils.getJSONString(0);
	}


	/**
	 * 执行用户更新头像的操作（不再使用）
	 *
	 * @param imageFile
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@LoginRequired
	public String doSetting(MultipartFile imageFile, Model model) {
		// 1: get file and suffix
		if (imageFile == null) {
			model.addAttribute("error", "您还没有选择图片!");
			return "/site/setting";
		}
		String fileName = imageFile.getOriginalFilename();
		// get file extension
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		if (StringUtils.isBlank(suffix)) {
			model.addAttribute("error", "文件的格式不正确!");
			return "/site/setting";
		}

		// 2: create random fileName
		fileName = CommunityUtils.getRandomUUID() + suffix;

		// 3: store to local path
		File upload = new File(uploadPath + "/" + fileName);
		try {
			// store the image (method given by MultipartFile)
			imageFile.transferTo(upload);
		} catch (IOException e) {
			log.error("上传文件失败: " + e.getMessage());
			throw new RuntimeException("上传文件失败,服务器发生异常!", e);
		}

		// 4: update user OBJ and user table
		User user = hostHolder.getUser();
		// http://localhost:85/community/user/header/random-fileName
		String newHeaderURL = domainPath + context + "/user/header/" + fileName;
		userService.updateHeader(user.getId(), newHeaderURL, user);
		// user.setHeaderUrl(newHeaderURL);       later update in TicketInterceptor by cookie

		return "redirect:/indexPage";
	}

	/**
	 * 获得用户头像并返回给前端页面（不完全不再使用）
	 *
	 * @param fileName
	 * @param response
	 */
	@RequestMapping(value = "header/{fileName}", method = RequestMethod.GET)
	public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		// 1: get localPath of Image
		fileName = uploadPath + "/" + fileName;
		// get suffix
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		response.setContentType("image/" + suffix);

		// 2: use stream to return image to front
		try (FileInputStream inputStream = new FileInputStream(
				fileName); OutputStream os = response.getOutputStream()) {
			byte[] buffer = new byte[1024];
			int b = 0;
			while ((b = inputStream.read(buffer)) != -1) {
				os.write(buffer, 0, b);
			}
		} catch (IOException e) {
			log.error("读取头像失败: " + e.getMessage());
		}
	}

	/**
	 * 执行更新密码操作
	 *
	 * @param oldPassword
	 * @param newPassword
	 * @param confirmPassword
	 * @param model
	 * @return
	 */
	@LoginRequired
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model) {
		if (StringUtils.isBlank(oldPassword)) {
			model.addAttribute("oldPwdMsg", "原始密码不为空");
			return "/site/setting";
		}

		User user = hostHolder.getUser();
		String salt = user.getSalt();
		oldPassword = CommunityUtils.md5(oldPassword + salt);
		if (!user.getPassword().equals(oldPassword)) {
			model.addAttribute("oldPwdMsg", "原始密码错误");
			return "/site/setting";
		}

		if (StringUtils.isBlank(newPassword) || StringUtils.isBlank(confirmPassword)) {
			model.addAttribute("newPwdMsg", "新设置的密码不能为空");
		}

		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("newPwdMsg", "两次密码不相等");
			return "/site/setting";
		}

		// same salt
		newPassword = CommunityUtils.md5(newPassword + salt);

		userService.updatePassword(user.getId(), newPassword, user);
		return "redirect:/indexPage";
	}

	/**
	 * 获得前端用户的简介页面
	 *
	 * @param userId
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
	public String getProfilePage(@PathVariable("userId") int userId, Model model) {
		User user = userService.selectById(userId);
		if (user == null) {
			throw new RuntimeException("该用户不存在!");
		}
		// 当前页面对应的用户
		model.addAttribute("user", user);
		// 点赞数量
		int likeCount = likeService.findUserLikeCount(userId);
		model.addAttribute("likeCount", likeCount);

		// 查询当前页面对应的用户关注的实体的数量
		long followeeCount = followService.findFolloweeCount(userId, ENTITY_TARGET_USER);
		model.addAttribute("followeeCount", followeeCount);
		// 查询当前页面用户的粉丝的数量
		long followerCount = followService.findFollowerCount(ENTITY_TARGET_USER, userId);
		model.addAttribute("followerCount", followerCount);

		// 查询当前线程用户是否已关注当前页面的用户
		boolean followed = false;
		if (hostHolder.getUser() != null) {
			// 不可能自我关注
			followed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TARGET_USER, userId);
		}
		model.addAttribute("hasFollowed", followed);

		return "/site/profile";
	}
}
