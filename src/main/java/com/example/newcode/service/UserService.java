package com.example.newcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface UserService extends IService<User> {
	List<User> selectByName(String name);

	User selectById(int id);

	List<User> selectByEmail(String email);

	Boolean insertUser(User user);

	Boolean updateStatus(int id, int status, User user);

	Boolean updateHeader(int id, String headerUrl, User user);

	Boolean updatePassword(int id, String password, User user);

	User getCache(int userId);

	User initCache(int userId);

	void clearCache(int userId);

	Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
