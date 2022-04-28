package com.example.newcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
	// 查询当前用户的会话列表,针对每个会话只返回一条最新的私信message实体
	Page<Message> selectConversations(int userID, int currentPage, int PageSize);

	// 查询当前用户的会话数量（接收方或者发送方）
	int selectConversationCount(int userId);

	// 查询某个会话所包含的私信列表
	Page<Message> selectLetters(String conversationId, int currentPage, int PageSize);

	// 查询某个会话所包含的私信数量
	int selectLetterCount(String conversationId);

	// 查询未读私信的数量（传入conversationId则是查询当前用户某一会话的未读私信数量
	// conversationId为null则查询当前用户所有未读私信数量）
	// 因为所有的私信都在一张表上
	int selectLetterUnreadCount(int userId, String conversationId);

	// 加入一个message
	Boolean insertMessage(Message message);

	// 通过messageID更新status状态
	Boolean updateStatus(List<Integer> ids, int status);

	// 查询某个主题下给某个用户的最新系统通知（最新的一个）
	Message selectLatestNotice(int userId, String topic);

	// 查询某个主题下给某个用户所有的系统通知数量
	int selectNoticeCount(int userId, String topic);

	// 查询[某个主题下]给某个用户的未读消息数量
	int selectNoticeUnreadCount(int userId, String topic);

	//  查询某个主题下给某个用户的所有系统通知（分页查询）
	Page<Message> selectNotices(int userId, String topic, int currentPage, int PageSize);
}
