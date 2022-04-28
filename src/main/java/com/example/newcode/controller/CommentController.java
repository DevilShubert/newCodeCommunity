package com.example.newcode.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.Comment;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.Event;
import com.example.newcode.event.EventProducer;
import com.example.newcode.service.CommentService;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

	@Autowired
	HostHolder hostHolder;

	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	CommentService commentService;

	@Autowired
	EventProducer eventProducer;

	/**
	 * 增加一条comment
	 */
	@RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
	@LoginRequired
	public String addComment(@PathVariable("discussPostId") int postID, Comment comment) {
		if (comment == null) {
			throw new IllegalArgumentException("参数不能为空!");
		}

		// comment实体默认会有entityType、entityId、content（targetId）
		// 对于targetId属性，如果默认是0，如果此条评论是对某个用户的回复，则targetId=1
		comment.setUserId(hostHolder.getUser().getId());
		comment.setCreateTime(new Date());
		comment.setStatus(0);

		// locate discussPost
		DiscussPost discussPost = discussPostService.selectDiscussPostsByPostID(postID);

		// do insert
		commentService.insertComment(comment, discussPost);

		// 触发评论事件
		Event event = new Event().setTopic(TOPIC_COMMENT).// 设置主题
				setUserId(hostHolder.getUser().getId()). // 设置事件发出者id
				setEntityType(comment.getEntityType()). // 1为ENTITY_TARGET_POST，2则是ENTITY_TARGET_COMMENT
				setEntityId(comment.getEntityId()). // post或comment对应的id
				setData("postId", postID);  // 对应该事件的帖子的id

		// 如果该comment是回帖，则发送系统通知
		if (comment.getEntityType() == ENTITY_TARGET_POST) {
			event.setEntityUserId(discussPost.getUserId()); // 存入帖子作者的id
		} else if (comment.getEntityType() == ENTITY_TARGET_COMMENT) {
			// 如果是那两种回复的comment
			Page<Comment> commentPage = commentService.selectCommentsByEntity(ENTITY_TARGET_COMMENT,
					comment.getEntityId(), 1, Integer.MAX_VALUE);
			List<Comment> records = commentPage.getRecords();
			Comment curComment = records.get(0);
			event.setEntityUserId(curComment.getUserId()); // 两种回复的comment的作者（也就是被评论的comment的作者的id）
		}
		eventProducer.fireEvent(event);

		// 如果是回帖，则要通过事件异步让ES数据更新
		if (comment.getEntityType() == ENTITY_TARGET_POST) {
			event = new Event().setTopic(TOPIC_PUBLISH).setUserId(comment.getUserId()).setEntityType(
					ENTITY_TARGET_POST).setEntityId(discussPost.getId());
			eventProducer.fireEvent(event);
		}
		return "redirect:/discuss/detail/" + postID;
	}
}
