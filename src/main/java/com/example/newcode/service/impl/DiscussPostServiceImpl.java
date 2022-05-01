package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.DiscussPostDao;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostDao, DiscussPost> implements DiscussPostService {

	@Autowired
	DiscussPostDao discussPostDao;

	@Autowired
	SensitiveFilter sensitiveFilter;

	/**
	 * 默认页面的分页查询，排序规则为：是否置顶、创建时间
	 *
	 * @param userID
	 * @param curPage
	 * @param pageSize
	 * @return com.baomidou.mybatisplus.core.metadata.IPage<com.example.newcode.entity.DiscussPost>
	 * @author JLian
	 * @date 2022/2/22 6:25 下午
	 */

	public IPage<DiscussPost> selectMapsPage(int userID, int curPage, int pageSize, int useScore) {
		Page<DiscussPost> postPage = new Page<>(curPage, pageSize);
		QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
		// 如果userID = 0，则这行eq不生效，userID != 0时才生效
		wrapper.eq(userID != 0, "user_id", userID);
		wrapper.le("status", 1);
		if (useScore == 0)
			wrapper.orderByDesc("type", "create_time");
		else
			wrapper.orderByDesc("type", "score", "create_time");
		return  discussPostDao.selectPage(postPage, wrapper);
	}

	/**
	 * @param userID
	 * @return java.lang.Integer
	 * @author JLian 带有条件的查询有多少条帖子
	 * @date 2022/2/22 6:25 下午
	 */

	@Override
	public DiscussPost selectDiscussPostsByUserID(int userID) {
		QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
		wrapper.eq(userID != 0, "user_id", userID);
		wrapper.le("status", 1);
		DiscussPost discussPost = discussPostDao.selectOne(wrapper);
		return discussPost;
	}

	@Override
	public DiscussPost selectDiscussPostsByPostID(int postID) {
		QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
		wrapper.eq(postID != 0, "id", postID);
		wrapper.le("status", 1);
		DiscussPost discussPost = discussPostDao.selectOne(wrapper);
		return discussPost;
	}

	@Override
	public Integer addDiscussPost(DiscussPost post) {
		if (post == null) {
			throw new IllegalArgumentException("参数不能为空!");
		}
		// 转义HTML标记
		post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
		post.setContent(HtmlUtils.htmlEscape(post.getContent()));
		// filter
		post.setTitle(sensitiveFilter.filter(post.getTitle()));
		post.setContent(sensitiveFilter.filter(post.getContent()));
		return discussPostDao.insert(post);
	}

	@Override
	public Boolean updateCommentCount(DiscussPost post, int count) {
		UpdateWrapper<DiscussPost> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", post.getId());
		post.setCommentCount(count);
		return discussPostDao.update(post, updateWrapper) > 0;
	}

	@Override
	public Boolean updateType(DiscussPost post, int type) {
		UpdateWrapper<DiscussPost> postUpdateWrapper = new UpdateWrapper<>();
		postUpdateWrapper.eq("id", post.getId());
		post.setType(type);
		return discussPostDao.update(post, postUpdateWrapper) > 0;
	}

	@Override
	public Boolean updateStatus(DiscussPost post, int status) {
		UpdateWrapper<DiscussPost> postUpdateWrapper = new UpdateWrapper<>();
		postUpdateWrapper.eq("id", post.getId());
		post.setStatus(status);
		return discussPostDao.update(post, postUpdateWrapper) > 0;
	}

	@Override
	public Boolean updateScore(DiscussPost post, double score) {
		UpdateWrapper<DiscussPost> postUpdateWrapper = new UpdateWrapper<>();
		postUpdateWrapper.eq("id", post.getId());
		post.setScore(score);
		return discussPostDao.update(post, postUpdateWrapper) > 0;
	}
}
