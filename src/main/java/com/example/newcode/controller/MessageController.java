package com.example.newcode.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.entity.Message;
import com.example.newcode.entity.MyPage;
import com.example.newcode.entity.User;
import com.example.newcode.service.MessageService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

	@Autowired
	MessageService messageService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	private UserService userService;

	/**
	 * 获取当前用户的会话列表
	 *
	 * @param model
	 * @param myPage
	 * @return
	 */
	@RequestMapping(path = "/letter/list", method = RequestMethod.GET)
	public String getLetterList(Model model, MyPage myPage) {
		myPage.setLimit(5);
		myPage.setPath("/letter/list");

		// query all conversation counts
		int userID = hostHolder.getUser().getId();
		int conversationCount = messageService.selectConversationCount(userID);
		myPage.setRows(conversationCount);

		// query all unRead letter counts
		int letterUnreadCount = messageService.selectLetterUnreadCount(userID, null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);

		// only store all conversation lists
		List<Map<String, Object>> conversations = new ArrayList<>();

		// query every latest private message in each conversation
		Page<Message> conversationListsPage = messageService.selectConversations(userID, myPage.getCurrent(),
				myPage.getLimit());

		myPage.setTotal((int) conversationListsPage.getPages());
		for (Message message : conversationListsPage.getRecords()) {
			HashMap<String, Object> hashMap = new HashMap<>();
			// store one message
			hashMap.put("conversation", message);
			// store all counts in one conversation
			hashMap.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
			// store conversation private message target
			int targetID = userID == message.getFromId() ? message.getToId() : message.getFromId();
			hashMap.put("target", userService.selectById(targetID));
			int conversationUnreadCounts = messageService.selectLetterUnreadCount(userID, message.getConversationId());
			hashMap.put("unreadCount", conversationUnreadCounts);
			conversations.add(hashMap);
		}

		model.addAttribute("conversations", conversations);
		model.addAttribute("page", myPage);

		// 查询系统通知未读消息数量
		int noticeUnreadCount = messageService.selectNoticeUnreadCount(userID, null);
		model.addAttribute("noticeUnreadCount", noticeUnreadCount);
		return "/site/letter";
	}

	/**
	 * 获取某一会话详情页
	 *
	 * @param conversationId 会话ID
	 * @param myPage
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
	public String getLetterDetail(@PathVariable("conversationId") String conversationId, MyPage myPage, Model model) {
		myPage.setLimit(5);
		myPage.setPath("/letter/detail/" + conversationId);
		//  查询这个会话当前页面下所包含的message列表
		Page<Message> messagePage = messageService.selectLetters(conversationId, myPage.getCurrent(),
				myPage.getLimit());

		// 设置总行数
		myPage.setRows((int) messagePage.getTotal());

		// 设置总页数
		myPage.setTotal((int) messagePage.getPages());

		// 得到当前会话所有的私信
		List<Message> messageLists = messagePage.getRecords();
		if (messageLists != null) {
			List<Map<String, Object>> letters = new ArrayList<>();
			for (Message message : messageLists) {
				HashMap<String, Object> hashMap = new HashMap<>();
				hashMap.put("letter", message);
				hashMap.put("fromUser", userService.selectById(message.getFromId()));
				letters.add(hashMap);
			}

			// 选出所有当前会话中需要更新状态为已读的私信message的id，也就是当前用户Id作为接收方的记录
			List<Message> needUpdate = messageService.selectLetters(conversationId, 1, Integer.MAX_VALUE).getRecords();
			List<Integer> updateIds = getLetterIds(needUpdate);
			if (updateIds.size() != 0) {
				messageService.updateStatus(updateIds, 1);
			}

			// 会话中消息实体
			model.addAttribute("letters", letters);
		}

		// 传递消息发送方user对象
		String[] ids = conversationId.split("_");
		int id0 = Integer.parseInt(ids[0]);
		int id1 = Integer.parseInt(ids[1]);
		if (hostHolder.getUser().getId() == id0) {
			User user = userService.selectById(id1);
			model.addAttribute("target", user);
		} else {
			User user = userService.selectById(id0);
			model.addAttribute("target", user);
		}
		// page实体
		model.addAttribute("page", myPage);
		return "/site/letter-detail";
	}

	/**
	 * 获取需要更新的messageID
	 *
	 * @param letterList
	 * @return
	 */
	private List<Integer> getLetterIds(List<Message> letterList) {
		// 这一步的逻辑是只更新接收方的ids，假设我们打开一个会话详情页之后，如果把所有的massageID都更新为已读
		// 则发送方最新收到的message状态status也会变已读，我们只想更新接收方的message为已读
		List<Integer> ids = new ArrayList<>();
		int userID = hostHolder.getUser().getId();
		for (Message message : letterList) {
			// 当前user作为接收方
			if (message.getToId() == userID && message.getStatus() == 0) {
				ids.add(message.getId());
			}
		}
		return ids;
	}

	/**
	 * 发送私信
	 *
	 * @param toName
	 * @param content
	 * @return
	 */
	@RequestMapping(path = "/letter/send", method = RequestMethod.POST)
	@ResponseBody
	public String sendLetter(String toName, String content) {
		List<User> selectByName = userService.selectByName(toName);
		if (StringUtils.isBlank(toName)) {
			return CommunityUtils.getJSONString(1, "目标用户不能为空!");
		}
		if (StringUtils.isBlank(content)) {
			return CommunityUtils.getJSONString(1, "文章内容不能为空!");
		}
		if (selectByName.size() == 0) {
			return CommunityUtils.getJSONString(1, "目标用户不存在");
		}
		Message message = new Message();
		// 当前user：消息发送者
		int fromUserID = hostHolder.getUser().getId();
		message.setFromId(fromUserID);
		// 目标对象：消息接受者
		int toUserID = selectByName.get(0).getId();
		message.setToId(toUserID);
		// 设置会话id，小的在前，大的在后
		if (message.getFromId() < message.getToId()) {
			message.setConversationId(message.getFromId() + "_" + message.getToId());
		} else {
			message.setConversationId(message.getToId() + "_" + message.getFromId());
		}
		message.setCreateTime(new Date());
		message.setContent(content);
		messageService.insertMessage(message);

		System.out.println("====");
		return CommunityUtils.getJSONString(0);
	}

	/**
	 * 通知列表页
	 */
	@RequestMapping(path = "/notice/list", method = RequestMethod.GET)
	public String getNoticeList(Model model) {
		User user = hostHolder.getUser();

		// 查询评论类通知
		Message message = messageService.selectLatestNotice(user.getId(), TOPIC_COMMENT);
		Map<String, Object> messageVO;
		if (message != null) {
			messageVO = new HashMap<>();
			messageVO.put("message", message);

			// 因为message表中的content内容被转义了的，所以要反转义
			String content = HtmlUtils.htmlUnescape(message.getContent());
			// HashMap格式详情见消费者如何存入message表中
			Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

			messageVO.put("user", userService.selectById((Integer) data.get("userId")));
			messageVO.put("entityType", data.get("entityType")); // 回帖 或 两种回复
			messageVO.put("entityId", data.get("entityId")); // 回帖 或 两种回复对应的id
			messageVO.put("postId", data.get("postId")); // 因为评论类(comment)必定会有个帖子，所以要返回这个参数

			int count = messageService.selectNoticeCount(user.getId(), TOPIC_COMMENT);
			messageVO.put("count", count);

			int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
			messageVO.put("unread", unread);
			model.addAttribute("commentNotice", messageVO);
		} else {
			model.addAttribute("commentNotice", null);
		}
		// 查询点赞类通知
		message = messageService.selectLatestNotice(user.getId(), TOPIC_LIKE);

		if (message != null) {
			messageVO = new HashMap<>();
			messageVO.put("message", message);
			String content = HtmlUtils.htmlUnescape(message.getContent());
			Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
			messageVO.put("user", userService.selectById((Integer) data.get("userId")));
			messageVO.put("entityType", data.get("entityType")); // 1表示目标是帖子，2表示目标是comment
			messageVO.put("entityId", data.get("entityId")); // 对应的实体id
			messageVO.put("postId", data.get("postId")); // 因为点赞也必定会有个帖子，所以要返回这个参数
			int count = messageService.selectNoticeCount(user.getId(), TOPIC_LIKE);
			messageVO.put("count", count);
			int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_LIKE);
			messageVO.put("unread", unread);
			model.addAttribute("likeNotice", messageVO);
		} else {
			model.addAttribute("likeNotice", null);
		}

		// 查询关注类通知
		message = messageService.selectLatestNotice(user.getId(), TOPIC_FOLLOW);

		if (message != null) {
			messageVO = new HashMap<>();
			messageVO.put("message", message);
			String content = HtmlUtils.htmlUnescape(message.getContent());
			Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

			messageVO.put("user", userService.selectById((Integer) data.get("userId")));
			messageVO.put("entityType", data.get("entityType"));
			messageVO.put("entityId", data.get("entityId"));

			int count = messageService.selectNoticeCount(user.getId(), TOPIC_FOLLOW);
			messageVO.put("count", count);

			int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
			messageVO.put("unread", unread);
			model.addAttribute("followNotice", messageVO);
		} else {
			model.addAttribute("followNotice", null);
		}

		// 查询未读消息数量
		int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);
		int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
		model.addAttribute("noticeUnreadCount", noticeUnreadCount);

		return "/site/notice";
	}

	/**
	 * 获取某一个通知的具体页面
	 */
	@RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
	public String getNoticeDetail(@PathVariable("topic") String topic, MyPage page, Model model) {
		User user = hostHolder.getUser();

		page.setLimit(5);
		page.setPath("/notice/detail/" + topic);
		//  查询这个topic当前页面下所包含的notices列表
		Page<Message> notices = messageService.selectNotices(user.getId(), topic, page.getCurrent(), page.getLimit());

		// 设置总行数
		page.setRows((int) notices.getTotal());

		// 设置总页数
		page.setTotal((int) notices.getPages());

		List<Message> noticeList = notices.getRecords();

		List<Map<String, Object>> noticeVoList = new ArrayList<>();
		if (noticeList != null) {
			for (Message notice : noticeList) {
				Map<String, Object> map = new HashMap<>();
				// 通知
				map.put("notice", notice);
				// 反转移内容
				String content = HtmlUtils.htmlUnescape(notice.getContent());
				// 解析content
				Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
				// 当前user
				map.put("user", userService.selectById((Integer) data.get("userId")));
				map.put("entityType", data.get("entityType"));
				map.put("entityId", data.get("entityId"));
				map.put("postId", data.get("postId"));
				// 事件触发的作者
				map.put("fromUser", userService.selectById(notice.getFromId()));
				noticeVoList.add(map);
			}
		}
		model.addAttribute("notices", noticeVoList);

		// 选出所有需要更新当前用户系统通知的状态为已读的数据，并更新
		List<Message> needUpdate = messageService.selectNotices(user.getId(), topic, 1, Integer.MAX_VALUE).getRecords();
		List<Integer> ids = getLetterIds(needUpdate);
		if (ids.size() != 0) {
			messageService.updateStatus(ids, 1);
		}

		// page实体
		model.addAttribute("page", page);
		// 对应topic
		model.addAttribute("topic", topic);
		return "site/notice-detail";
	}
}
