package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.MessageDao;
import com.example.newcode.entity.Message;
import com.example.newcode.service.MessageService;
import com.example.newcode.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageDao, Message> implements MessageService {

	@Autowired
	MessageDao messageDao;

	@Autowired
	SensitiveFilter sensitiveFilter;

	@Override
	public Page<Message> selectConversations(int userID, int currentPage, int pageSize) {
		QueryWrapper<Message> wrapper = new QueryWrapper<>();
		// sub query sql
		String subQuery = "select max(id) from message where status != 2 and from_id != 1 and (from_id = " + userID
				+ " or to_id = " + userID + ") group by conversation_id";
		wrapper.inSql("id", subQuery);
		wrapper.orderByDesc("id");

		Page<Message> page = new Page<>(currentPage, pageSize);
		Page<Message> messagePage = messageDao.selectPage(page, wrapper);
		return messagePage;
	}

	@Override
	public int selectConversationCount(int userId) {
		QueryWrapper<Message> wrapper = new QueryWrapper<>();
		// 表示当前分组下最大的记录id
		String subQuery = "select max(id) from message where status != 2 and from_id != 1 and (from_id = " + userId
				+ " or to_id = " + userId + ") group by conversation_id";
		wrapper.inSql("id", subQuery);
		Integer selectCount = messageDao.selectCount(wrapper);
		return selectCount;
	}

	@Override
	public Page<Message> selectLetters(String conversationId, int currentPage, int PageSize) {
		QueryWrapper<Message> wrapper = new QueryWrapper<>();
		wrapper.ne("status", 2);
		wrapper.ne("from_id", 1);
		// 查询核心conversationId，靠这个找到所有的
		wrapper.eq("conversation_id", conversationId);
		wrapper.orderByDesc("id");

		Page<Message> page = new Page<>(currentPage, PageSize);
		Page<Message> messagePage = messageDao.selectPage(page, wrapper);
		return messagePage;
	}

	@Override
	public int selectLetterCount(String conversationId) {
		QueryWrapper<Message> wrapper = new QueryWrapper<>();
		wrapper.ne("status", 2);
		wrapper.ne("from_id", 1);
		wrapper.eq("conversation_id", conversationId);
		return messageDao.selectCount(wrapper);
	}

	@Override
	public int selectLetterUnreadCount(int userId, String conversationId) {
		QueryWrapper<Message> wrapper = new QueryWrapper<>();
		wrapper.eq("status", 0);
		wrapper.ne("from_id", 1);
		wrapper.eq("to_id", userId);
		if (conversationId != null) {
			wrapper.eq("conversation_id", conversationId);
		}
		Integer count = messageDao.selectCount(wrapper);
		return count;
	}

	@Override
	public Boolean insertMessage(Message message) {
		message.setContent(HtmlUtils.htmlEscape(message.getContent()));
		message.setContent(sensitiveFilter.filter(message.getContent()));
		return messageDao.insert(message) > 0;
	}

	@Override
	public Boolean updateStatus(List<Integer> ids, int status) {
		UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
		updateWrapper.in("id", ids);
		updateWrapper.set("status", status);
		return messageDao.update(null, updateWrapper) > 0;
	}

	@Override
	public Message selectLatestNotice(int userId, String topic) {
		QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
		queryWrapper.ne("status", 2);
		queryWrapper.eq("from_id", 1);
		queryWrapper.eq("to_id", userId);
		queryWrapper.eq("conversation_id", topic);
		queryWrapper.orderByDesc("id");
		queryWrapper.last("limit 1"); // 目的是查询单条
		return messageDao.selectOne(queryWrapper);
	}

	@Override
	public int selectNoticeCount(int userId, String topic) {
		QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
		queryWrapper.ne("status", 2);
		queryWrapper.eq("from_id", 1);
		queryWrapper.eq("to_id", userId);
		queryWrapper.eq("conversation_id", topic);
		return messageDao.selectCount(queryWrapper);
	}

	@Override
	public int selectNoticeUnreadCount(int userId, String topic) {
		QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("status", 0);
		queryWrapper.eq("from_id", 1);
		queryWrapper.eq("to_id", userId);
		if (topic != null) {
			// 说明查询某一特定的消息未读消息
			queryWrapper.eq("conversation_id", topic);
			int count = messageDao.selectCount(queryWrapper);
			System.out.println(1);
			return count;
		} else {
			// 查询所有未读消息
			int count = messageDao.selectCount(queryWrapper);
			System.out.println(1);
			return count;
		}
	}

	@Override
	public Page<Message> selectNotices(int userId, String topic, int currentPage, int pageSize) {
		QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
		queryWrapper.ne("status", 2);
		queryWrapper.eq("from_id", 1);
		queryWrapper.eq("to_id", userId);
		queryWrapper.eq("conversation_id", topic);
		queryWrapper.orderByDesc("create_time");
		Page<Message> page = new Page<>(currentPage, pageSize);
		return messageDao.selectPage(page, queryWrapper);
	}
}
