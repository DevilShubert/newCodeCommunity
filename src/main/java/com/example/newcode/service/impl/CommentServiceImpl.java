package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.CommentDao;
import com.example.newcode.entity.Comment;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.CommentService;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.util.SensitiveFilter;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService, CommunityConstant {
	@Autowired
	CommentDao commentDao;

	@Autowired
	SensitiveFilter sensitiveFilter;

	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	CommentService commentService;

	@Override
	public int selectCountByEntity(int entityType, int entityId) {
		QueryWrapper<Comment> wrapper = new QueryWrapper<>();
		wrapper.eq("entity_type", entityType);
		wrapper.eq("entity_id", entityId);
		wrapper.eq("status", 0);
		return commentDao.selectCount(wrapper);
	}

	@Override
	public Page<Comment> selectCommentsByEntity(int entityType, int entityId, int curPage, int pageSize) {
		Page<Comment> postPage = new Page<>(curPage, pageSize);
		QueryWrapper<Comment> wrapper = new QueryWrapper<>();
		wrapper.eq("entity_type", entityType);
		wrapper.eq("entity_id", entityId);
		wrapper.eq("status", 0);
		Page<Comment> page = commentDao.selectPage(postPage, wrapper);
		return page;
	}

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public Boolean insertComment(Comment comment, DiscussPost discussPost) {

		// sensitive words filter
		// 使用springweb提供的工具类转义<和>
		comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
		comment.setContent(sensitiveFilter.filter(comment.getContent()));

		// insert
		int insertNum = commentDao.insert(comment);

		// 如果是对帖子
		if (comment.getEntityType() == ENTITY_TARGET_POST) {
			// after insert for certain EntityType and EntityId's comments number will add
			int count = commentService.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
			discussPostService.updateCommentCount(discussPost, count);

			// 触发事件，修改ES中的帖子
		}
		return insertNum > 0;
	}
}
