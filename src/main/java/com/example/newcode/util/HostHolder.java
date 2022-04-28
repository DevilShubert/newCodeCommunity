package com.example.newcode.util;

import com.example.newcode.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用于存储当前线程的变量
 */

@Component
public class HostHolder {
	// 当前线程
	private ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();

	public void setUser(User user) {
		// 如果重复set则会覆盖之前存的user
		this.userThreadLocal.set(user);
	}

	public User getUser() {
		return userThreadLocal.get();
	}

	public void clear() {
		userThreadLocal.remove();
	}
}
