package com.example.newcode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.DiscussPost;

public interface DiscussPostService extends IService<DiscussPost> {
	/**
	 * @param userID   用户id，作用是查找某一用户的帖子，如果是查找所有帖子则id=0
	 * @param curPage  当前页号
	 * @param pageSize 一页的大小
	 * @param useScore 排序顺序是否加上用户分数
	 * @return
	 */
	IPage<DiscussPost> selectMapsPage(int userID, int curPage, int pageSize, int useScore);

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

	/**
	 * 更新帖子的分数
	 *
	 * @param post
	 * @param score
	 * @return
	 */
	Boolean updateScore(DiscussPost post, double score);
}
