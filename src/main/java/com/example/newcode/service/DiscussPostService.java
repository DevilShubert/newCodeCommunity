package com.example.newcode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.DiscussPost;

public interface DiscussPostService extends IService<DiscussPost> {
	/**
	 * select all posts by pagination
	 *
	 * @param userID   用户id
	 * @param curPage  当前页号
	 * @param pageSize 一页的大小
	 * @return
	 */
	IPage<DiscussPost> selectMapsPage(int userID, int curPage, int pageSize);

	/**
	 * select one post by userID
	 *
	 * @param userID 用户ID
	 * @return
	 */
	DiscussPost selectDiscussPostsByUserID(int userID);

	/**
	 * select one post by postID
	 *
	 * @param postID 用户ID
	 * @return
	 */
	DiscussPost selectDiscussPostsByPostID(int postID);

	/**
	 * add new post
	 *
	 * @param post 帖子实体
	 * @return
	 */
	Integer addDiscussPost(DiscussPost post);

	/**
	 * 更新comment数量
	 *
	 * @param post
	 * @param count
	 * @return
	 */
	Boolean updateCommentCount(DiscussPost post, int count);

	/**
	 * 更新帖子的类型 0-普通、1-置顶
	 *
	 * @param post
	 * @param type
	 * @return
	 */
	Boolean updateType(DiscussPost post, int type);

	/**
	 * 更新帖子状态 0-正常; 1-精华; 2-拉黑
	 *
	 * @param post
	 * @param status
	 * @return
	 */
	Boolean updateStatus(DiscussPost post, int status);
}
