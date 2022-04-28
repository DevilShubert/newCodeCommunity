package com.example.newcode.service;

import java.util.List;
import java.util.Map;

public interface FollowService {
	/**
	 * @param userId     当前线程的用户id（）
	 * @param entityType 当前用户关注的实体类型：用户、评论、帖子（entityType分别为3、2、1）
	 * @param entityId   对应类型的实体id
	 */
	void follow(int userId, int entityType, int entityId);

	void unfollow(int userId, int entityType, int entityId);

	/**
	 * @param userId 当前页面用户
	 * @return
	 */
	List<Map<String, Object>> findFollowees(int userId, int offset, int limit);

	/**
	 * 获取有哪些用户关注了当前用户
	 *
	 * @param userId
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<Map<String, Object>> findFollowers(int userId, int offset, int limit);

	/**
	 * 查询（用户）关注的实体的数量
	 *
	 * @param userId
	 * @param entityType
	 * @return
	 */
	long findFolloweeCount(int userId, int entityType);

	/**
	 * 查询（用户）实体的粉丝的数量
	 *
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	long findFollowerCount(int entityType, int entityId);

	/**
	 * 查询当前用户是否已关注该实体
	 *
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	boolean hasFollowed(int userId, int entityType, int entityId);
}
