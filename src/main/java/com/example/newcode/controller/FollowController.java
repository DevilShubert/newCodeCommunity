package com.example.newcode.controller;

import com.example.newcode.entity.Event;
import com.example.newcode.entity.MyPage;
import com.example.newcode.entity.User;
import com.example.newcode.event.EventProducer;
import com.example.newcode.service.FollowService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

	@Autowired
	FollowService followService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	UserService userService;

	@Autowired
	private EventProducer eventProducer;

	/**
	 * 执行关注事件
	 *
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	@RequestMapping(value = "/follow", method = RequestMethod.POST)
	@ResponseBody
	public String follow(int entityType, int entityId) {
		User user = hostHolder.getUser();
		followService.follow(user.getId(), entityType, entityId);
		// 触发关注事件
		Event event = new Event().setTopic(TOPIC_FOLLOW). // 关注主题
				setUserId(hostHolder.getUser().getId()). // // 关注别人的人的id
				setEntityType(entityType). // 关注对象（暂时就只有人， 也就是3）
				setEntityId(entityId). // 被关注的人的id
				setEntityUserId(entityId); // 被关注的人的id（因为关注目前只能是关注人）
		eventProducer.fireEvent(event);
		return CommunityUtils.getJSONString(0, "已关注！");
	}

	/**
	 * 取消关注
	 *
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	@RequestMapping(value = "/unfollow", method = RequestMethod.POST)
	@ResponseBody
	public String unfollow(int entityType, int entityId) {
		User user = hostHolder.getUser();
		followService.unfollow(user.getId(), entityType, entityId);
		return CommunityUtils.getJSONString(0, "已取消关注！");
	}

	/**
	 * 查询当前用户关注的实体的数量与实体内容
	 *
	 * @param userId
	 * @param page
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
	public String getFollowees(@PathVariable("userId") int userId, MyPage page, Model model) {
		User user = userService.selectById(userId);
		if (user == null) {
			throw new RuntimeException("该用户不存在!");
		}

		model.addAttribute("user", user);
		page.setLimit(5);
		page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TARGET_USER));
		page.setPath("/followees/" + userId);
		// 设置总页数
		if (page.getRows() % page.getLimit() == 0) {
			page.setTotal(page.getRows() / page.getLimit());
		} else {
			page.setTotal(page.getRows() / page.getLimit() + 1);
		}

		List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
		if (followees != null) {
			for (Map<String, Object> map : followees) {
				User u = (User) map.get("user");
				// 单独判断当前线程是否关注了此用户，加入没有登录则是FALSE
				map.put("hasFollowed", hasFollowed(u.getId()));
			}
		}
		model.addAttribute("page", page);
		model.addAttribute("users", followees);
		return "/site/followee";
	}

	/**
	 * 查询当前当前用户所拥有的粉丝数量与粉丝内容
	 *
	 * @param userId
	 * @param page
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
	public String getFollowers(@PathVariable("userId") int userId, MyPage page, Model model) {
		User user = userService.selectById(userId);
		if (user == null) {
			throw new RuntimeException("该用户不存在!");
		}

		model.addAttribute("user", user);
		page.setLimit(5);
		page.setRows((int) followService.findFollowerCount(ENTITY_TARGET_USER, userId));
		page.setPath("/followers/" + userId);
		// 设置总页数
		if (page.getRows() % page.getLimit() == 0) {
			page.setTotal(page.getRows() / page.getLimit());
		} else {
			page.setTotal(page.getRows() / page.getLimit() + 1);
		}

		List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
		if (followers != null) {
			for (Map<String, Object> map : followers) {
				User u = (User) map.get("user");
				// 单独判断当前线程是否关注了此用户，加入没有登录则是FALSE
				map.put("hasFollowed", hasFollowed(u.getId()));
			}
		}
		model.addAttribute("page", page);
		model.addAttribute("users", followers);
		return "/site/follower";
	}

	private boolean hasFollowed(int userId) {
		if (hostHolder.getUser() == null) {
			return false;
		}
		return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TARGET_USER, userId);
	}

}
