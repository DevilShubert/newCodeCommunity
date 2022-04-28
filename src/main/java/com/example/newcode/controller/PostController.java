package com.example.newcode.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.*;
import com.example.newcode.event.EventProducer;
import com.example.newcode.service.CommentService;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.LikeService;
import com.example.newcode.service.UserService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class PostController implements CommunityConstant {
	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	UserService userService;

	@Autowired
	CommentService commentService;

	@Autowired
	LikeService likeService;

	@Autowired
	ElasticsearchService elasticsearchService;

	@Autowired
	EventProducer eventProducer;

	/**
	 * 异步插入一条帖子
	 *
	 * @param title
	 * @param content
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	@LoginRequired
	public String insertPost(String title, String content) {
		User user = hostHolder.getUser();
		DiscussPost post = new DiscussPost();
		post.setUserId(user.getId());
		post.setTitle(title);
		post.setContent(content);
		post.setType(0);
		post.setStatus(0);
		post.setCreateTime(new Date());
		post.setCommentCount(0);
		post.setScore(0.0);
		discussPostService.addDiscussPost(post);

		// 通过事件，异步向ES插入帖子
		Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(user.getId()).setEntityType(
				ENTITY_TARGET_POST).setEntityId(post.getId());
		// 发送事件
		eventProducer.fireEvent(event);

		// In case of error, it will be handled uniformly in the future
		return CommunityUtils.getJSONString(0, "发布成功!");
	}

	/**
	 * 查看一条帖子的详情页面
	 *
	 * @param postId
	 * @param model
	 * @param myPage
	 * @return
	 */
	@RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
	public String getDiscussPost(@PathVariable("discussPostId") int postId, Model model, MyPage myPage) {

		// 帖子
		DiscussPost post = discussPostService.selectDiscussPostsByPostID(postId);
		int userId = post.getUserId();

		// 作者
		User user = userService.selectById(userId);
		model.addAttribute("post", post);
		model.addAttribute("user", user);

		// 查询点赞数量
		long postLikeCount = likeService.findEntityLikeCount(ENTITY_TARGET_POST, postId);
		model.addAttribute("likeCount", postLikeCount);

		// 查询当前用户的点赞状态
		int postLikeStatus = hostHolder.getUser() == null ?
				0 :
				likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TARGET_POST, postId);
		model.addAttribute("likeStatus", postLikeStatus);

		// 设置帖子单页评论数量
		myPage.setLimit(5);
		// 设置访问路径：/detail/{discussPostId}
		myPage.setPath("/discuss/detail/" + postId);
		// 拿到对于帖子的评论数
		myPage.setRows(post.getCommentCount());

		// 包装对帖子的评论
		List<Map<String, Object>> commentVoList = new ArrayList<>();

		// 对帖子的评论
		Page<Comment> commentPage = commentService.selectCommentsByEntity(ENTITY_TARGET_POST, postId,
				myPage.getCurrent(), myPage.getLimit());

		List<Comment> commentList = commentPage.getRecords();
		// 设置总页数
		myPage.setTotal((int) commentPage.getPages());
		if (commentList != null) {
			for (Comment comment : commentList) {
				HashMap<String, Object> commentsVo = new HashMap<>();
				commentsVo.put("comment", comment);
				commentsVo.put("user", userService.selectById(comment.getUserId()));

				// 统计每个commentsVo的点赞数量
				long CommentLikeCount = likeService.findEntityLikeCount(ENTITY_TARGET_COMMENT, comment.getId());
				commentsVo.put("likeCount", CommentLikeCount);

				// 统计每个commentsVo的点赞状态
				int commentLikeStatus = hostHolder.getUser() == null ?
						0 :
						likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TARGET_COMMENT,
								comment.getId());
				commentsVo.put("likeStatus", commentLikeStatus);

				List<Map<String, Object>> replyVoList = new ArrayList<>();
				// 帖子中评论的回复comment列表（对于帖子中评论的回复comment就不再需要分页的操作）
				Page<Comment> replayPages = commentService.selectCommentsByEntity(ENTITY_TARGET_COMMENT,
						comment.getId(), 1, Integer.MAX_VALUE);
				List<Comment> replayList = replayPages.getRecords();

				// 包装回复列表
				if (replayList != null) {
					for (Comment replayComment : replayList) {
						HashMap<String, Object> replayVo = new HashMap<String, Object>();
						// 评论的comment实体
						replayVo.put("replay", replayComment);
						// replayComment的作者
						replayVo.put("user", userService.selectById(replayComment.getUserId()));
						// replayComment回复的目标：是对用户回复还是对帖子的评论回复
						User target = (replayComment.getTargetId() != 0) ?
								userService.selectById(replayComment.getTargetId()) :
								null;

						replayVo.put("target", target);

						// 得到回复的点赞数量count
						long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TARGET_COMMENT,
								replayComment.getId());
						replayVo.put("likeCount", replyLikeCount);

						// 得到回复的点赞状态
						int replyLikeStatus = hostHolder.getUser() == null ?
								0 :
								likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TARGET_COMMENT,
										replayComment.getId());
						replayVo.put("likeStatus", replyLikeStatus);

						replyVoList.add(replayVo);
					}
				}
				commentsVo.put("replays", replyVoList);
				// 回复（评论的comment）数量
				commentsVo.put("replayCount",
						commentService.selectCountByEntity(ENTITY_TARGET_COMMENT, comment.getId()));
				commentVoList.add(commentsVo);
			}
		}
		model.addAttribute("comments", commentVoList);
		model.addAttribute("page", myPage);
		return "/site/discuss-detail";
	}

	/**
	 * 执行帖子的置顶操作
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(path = "/top", method = RequestMethod.POST)
	@ResponseBody
	public String setTop(int id) {
		DiscussPost topPost = discussPostService.selectDiscussPostsByPostID(id);
		discussPostService.updateType(topPost, 1);

		// 触发发帖事件
		Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId()).setEntityType(
				ENTITY_TARGET_POST).setEntityId(id);

		eventProducer.fireEvent(event);

		return CommunityUtils.getJSONString(0);
	}

	/**
	 * 执行帖子的加精操作
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(path = "/wonderful", method = RequestMethod.POST)
	@ResponseBody
	public String setWonderful(int id) {
		DiscussPost wonderfulPost = discussPostService.selectDiscussPostsByPostID(id);
		discussPostService.updateStatus(wonderfulPost, 1);
		// 触发发帖事件
		Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId()).setEntityType(
				ENTITY_TARGET_POST).setEntityId(id);
		eventProducer.fireEvent(event);

		return CommunityUtils.getJSONString(0);
	}

	/**
	 * 执行删除帖子的操作
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(path = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String setDelete(int id) {
		DiscussPost deletePost = discussPostService.selectDiscussPostsByPostID(id);
		if (hostHolder.getUser().getId() == deletePost.getUserId() || hostHolder.getUser().getType() == 1) {
			// 只有当前帖子的用户和管理员才能执行删除操作
			discussPostService.updateStatus(deletePost, 2);

			// 触发删帖事件
			Event event = new Event().setTopic(TOPIC_DELETE).setUserId(hostHolder.getUser().getId()).setEntityType(
					ENTITY_TARGET_POST).setEntityId(id);
			eventProducer.fireEvent(event);

			return CommunityUtils.getJSONString(0);
		} else {
			// 没有删除
			return CommunityUtils.getJSONString(403, "你没有访问此功能的权限!");
		}

	}
}
