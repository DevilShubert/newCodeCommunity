package com.example.newcode.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.MyPage;
import com.example.newcode.entity.User;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.LikeService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	UserService userService;

	@Autowired
	LikeService likeService;

	@Autowired
	HostHolder hostHolder;

	/**
	 * 返回主页面内容（主要是帖子列表）
	 *
	 * @param model
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/indexPage", method = RequestMethod.GET)
	public String getIndexPage(Model model, MyPage page) {
		// MVC帮忙实现了类Mypage
		// 当第一次访问indexPage页面时，是没有提交任何page数据的，但是MVC会自动实例化一个对象
		IPage<DiscussPost> postIPage = discussPostService.selectMapsPage(0, page.getCurrent(), page.getLimit());

		// 设置总记录数
		int size = postIPage.getRecords().size();
		page.setRows(size);

		// 设置总页数
		page.setTotal((int) postIPage.getPages());

		// 设置访问路径
		page.setPath("/indexPage");

		User curUser = hostHolder.getUser();

		List<Map<String, Object>> discussPosts = new ArrayList<>();
		List<DiscussPost> postList = postIPage.getRecords();
		if (postList.size() != 0) {
			for (DiscussPost post : postList) {
				Map<String, Object> map = new HashMap<>();
				map.put("post", post);
				User user = userService.selectById(post.getUserId());
				map.put("user", user);

				// 得到每个帖子的点赞数
				long likeCount = likeService.findEntityLikeCount(ENTITY_TARGET_POST, post.getId());
				map.put("likeCount", likeCount);

				// 如果当前用户是登录状态
				if (curUser != null) {
					int likeStatus = likeService.findEntityLikeStatus(curUser.getId(), ENTITY_TARGET_POST,
							post.getId());
					map.put("likeStatus", likeStatus);
				} else {
					map.put("likeStatus", 0);
				}
				discussPosts.add(map);
			}
		}
		model.addAttribute("discussPosts", discussPosts);
		model.addAttribute("page", page);
		return "index";
	}

	@RequestMapping(path = "/denied", method = RequestMethod.GET)
	public String getDeniedPage() {
		return "/error/404";
	}
}
