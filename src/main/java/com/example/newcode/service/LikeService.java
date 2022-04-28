package com.example.newcode.service;

public interface LikeService {

	/**
	 * do Like
	 *
	 * @param userId       点赞用户
	 * @param entityType   被点赞实体类型
	 * @param entityId     被点赞实体id
	 * @param entityUserId 被点赞用户
	 */
	void like(int userId, int entityType, int entityId, int entityUserId);

	/**
	 * Count the number of likes
	 *
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	long findEntityLikeCount(int entityType, int entityId);

	/**
	 * 当前用户对当前entityType种类型下，对应id内容的点赞与否
	 *
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	int findEntityLikeStatus(int userId, int entityType, int entityId);

	/**
	 * 获取某个用户的被点赞数
	 *
	 * @param entityUserId
	 * @return
	 */
	int findUserLikeCount(int entityUserId);

}
