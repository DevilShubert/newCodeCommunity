package com.example.newcode.service.impl;

import com.example.newcode.service.LikeService;
import com.example.newcode.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
	@Autowired
	RedisUtils redisUtils;

	@Autowired
	@Qualifier("newCodeRedisTemplateConfig")
	RedisTemplate<String, Object> redisTemplate;

	@Override
	public void like(int userId, int entityType, int entityId, int entityUserId) {
		// 必须使用事务，因为要保证两个访问数据库操作的原子性
		redisTemplate.execute(new SessionCallback<Object>() {
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				// 获得某个被点赞实体的setName
				String entityLikeKey = redisUtils.getEntityLikeKey(entityType, entityId);
				// 某个被点赞实体对应的用户的key
				String userLikeKey = redisUtils.getUserLikeKey(entityUserId);
				// 判断当前用户是否给这个被点赞实体点赞过
				boolean isMember = redisUtils.sIsMember(entityLikeKey, userId);

				operations.multi();
				if (isMember) {
					redisUtils.setRemove(entityLikeKey, userId);
					// 被点赞实体对应的用户总赞数减一（再次点赞时则取消点赞）
					redisUtils.decrement(userLikeKey);
				} else {
					redisUtils.sSet(entityLikeKey, userId);
					redisUtils.increment(userLikeKey);
				}
				return operations.exec();
			}
		});
	}

	@Override
	public long findEntityLikeCount(int entityType, int entityId) {
		String entityLikeKey = redisUtils.getEntityLikeKey(entityType, entityId);
		return redisUtils.sGetSetSize(entityLikeKey);
	}

	@Override
	public int findEntityLikeStatus(int userId, int entityType, int entityId) {
		String entityLikeKey = redisUtils.getEntityLikeKey(entityType, entityId);
		boolean isMember = redisUtils.sIsMember(entityLikeKey, userId);
		// 1表示已点赞，2表示未点赞
		return isMember ? 1 : 0;
	}

	@Override
	public int findUserLikeCount(int entityUserId) {
		// 某个被点赞实体对应的用户的key
		String userLikeKey = redisUtils.getUserLikeKey(entityUserId);
		Object o = redisUtils.get(userLikeKey);
		if (o == null)
			return 0;
		else {
			String count = o.toString();
			return Integer.valueOf(count);
		}

	}

}
