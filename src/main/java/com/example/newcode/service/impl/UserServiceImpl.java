package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.UserDao;
import com.example.newcode.entity.User;
import com.example.newcode.service.UserService;
import com.example.newcode.util.RedisUtils;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService, CommunityConstant {

	@Autowired
	private UserDao userDao;

	@Autowired
	RedisUtils redisUtils;

	@Autowired
	UserService userService;

	@Override
	public User selectById(int id) {
		// 查询user时优化，后现在缓存中查
		String userKey = RedisUtils.getUserKey(id);
		// 注意这里会有并发问题，应该加锁
		User user = userService.getCache(id);
		// 缓存是否命中
		if (user == null) {
			user = initCache(id);
		}
		return user;
	}

	@Override
	public List<User> selectByName(String name) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("username", name);
		List<User> users = userDao.selectByMap(map);
		return users;
	}

	@Override
	public List<User> selectByEmail(String email) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("email", email);
		List<User> users = userDao.selectByMap(map);
		return users;
	}

	@Override
	public Boolean insertUser(User user) {
		return userDao.insert(user) > 0;
	}

	@Override
	public Boolean updateStatus(int id, int status, User user) {
		UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id);
		user.setStatus(status);
		Boolean flag = userDao.update(user, updateWrapper) > 0;
		userService.clearCache(id);
		return flag;
	}

	@Override
	public Boolean updateHeader(int id, String headerUrl, User user) {
		UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id);
		user.setHeaderUrl(headerUrl);
		Boolean flag = userDao.update(user, updateWrapper) > 0;
		userService.clearCache(id);
		return flag;
	}

	@Override
	public Boolean updatePassword(int id, String password, User user) {
		UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id);
		user.setPassword(password);
		Boolean flag = userDao.update(user, updateWrapper) > 0;
		userService.clearCache(id);
		return flag;
	}

	@Override
	public User getCache(int userId) {
		String userKey = RedisUtils.getUserKey(userId);
		User user = (User) redisUtils.get(userKey);
		return user;
	}

	@Override
	public User initCache(int userId) {
		User user = userDao.selectById(userId);
		String userKey = RedisUtils.getUserKey(userId);
		// 设置user在redis缓存中的时间为1h
		redisUtils.setWithExpire(userKey, user, 60 * 60, TimeUnit.SECONDS);
		return user;
	}

	@Override
	public void clearCache(int userId) {
		String userKey = RedisUtils.getUserKey(userId);
		redisUtils.del(userKey);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
		User user = this.selectById(userId);

		List<GrantedAuthority> list = new ArrayList<>();
		list.add(new GrantedAuthority() {

			@Override
			public String getAuthority() {
				switch (user.getType()) {
				case 1:
					return AUTHORITY_ADMIN;
				case 2:
					return AUTHORITY_MODERATOR;
				default:
					return AUTHORITY_USER;
				}
			}
		});
		return list;
	}
}
