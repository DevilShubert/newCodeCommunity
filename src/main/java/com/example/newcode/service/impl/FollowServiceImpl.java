package com.example.newcode.service.impl;

import com.example.newcode.entity.User;
import com.example.newcode.service.FollowService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.RedisUtils;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {

	@Autowired
	RedisUtils redisUtils;

	@Autowired
	UserService userService;

	@Autowired
	@Qualifier("newCodeRedisTemplateConfig")
	RedisTemplate<String, Object> redisTemplate;

	@Override
	public void follow(int userId, int entityType, int entityId) {
		redisTemplate.execute(new SessionCallback<Object>() {
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				// 当前用户关注的内容对应的key
				String followeeKey = RedisUtils.getFolloweeKey(userId, entityType);
				// 当前实体拥有的粉丝对应的key（粉丝只可能是user）
				String followerKey = RedisUtils.getFollowerKey(entityType, entityId);

				// 开启事务
				redisTemplate.multi();
				// 站在当前user角度看修改自己关注的东西
				redisUtils.zAdd(followeeKey, entityId, System.currentTimeMillis());
				// 站在当前user度看修改关注目标的粉丝群体
				redisUtils.zAdd(followerKey, userId, System.currentTimeMillis());
				return operations.exec();
			}
		});
	}

	@Override
	public void unfollow(int userId, int entityType, int entityId) {
		redisTemplate.execute(new SessionCallback<Object>() {
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				// 当前用户关注的内容对应的key
				String followeeKey = RedisUtils.getFolloweeKey(userId, entityType);
				// 当前实体拥有的粉丝对应的key（粉丝只可能是user）
				String followerKey = RedisUtils.getFollowerKey(entityType, entityId);
				// 开启事务
				redisTemplate.multi();
				// 站在当前user角度修改自己关注的东西
				redisUtils.zRemove(followeeKey, entityId);
				// 站在当前实体角度看修改粉丝
				redisUtils.zRemove(followerKey, userId);
				return operations.exec();
			}
		});
	}

	@Override
	public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
		String followeeKey = RedisUtils.getFolloweeKey(userId, ENTITY_TARGET_USER);
		Set<Object> targetIds = redisUtils.zSetRange(followeeKey, offset, limit);
		if (targetIds == null) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<>();

		for (Object targetId : targetIds) {
			Map<String, Object> map = new HashMap<>();
			User user = userService.selectById((Integer) targetId);
			map.put("user", user);
			Object followTime = redisUtils.score(followeeKey, (Integer) targetId);
			Double time = (Double) followTime;
			map.put("followTime", new Date(time.longValue()));
			list.add(map);
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
		String followerKey = RedisUtils.getFollowerKey(ENTITY_TARGET_USER, userId);
		Set<Object> targetIds = redisUtils.zSetRange(followerKey, offset, limit);
		if (targetIds == null) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<>();

		for (Object targetId : targetIds) {
			Map<String, Object> map = new HashMap<>();
			User user = userService.selectById((Integer) targetId);
			map.put("user", user);
			Object followTime = redisUtils.score(followerKey, (Integer) targetId);
			Double time = (Double) followTime;
			map.put("followTime", new Date(time.longValue()));
			list.add(map);
		}
		return list;
	}

	@Override
	public long findFolloweeCount(int userId, int entityType) {
		String followeeKey = RedisUtils.getFolloweeKey(userId, entityType);
		return redisUtils.zCard(followeeKey);
	}

	@Override
	public long findFollowerCount(int entityType, int entityId) {
		String followerKey = RedisUtils.getFollowerKey(entityType, entityId);
		return redisUtils.zCard(followerKey);
	}

	@Override
	public boolean hasFollowed(int userId, int entityType, int entityId) {
		String followeeKey = RedisUtils.getFolloweeKey(userId, entityType);
		// 只要能查到，就说明关注了
		return redisUtils.score(followeeKey, entityId) != null;
	}
}
