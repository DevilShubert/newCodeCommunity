package com.example.newcode.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
	private String topic; // 对应事件
	private int userId;    // 事件发起者
	private int entityType;    // 事件类型（如果是评论，则1位ENTITY_TARGET_POST，2则是ENTITY_TARGET_COMMENT，其他的则另当别论）
	private int entityId;    // 对应事件的id（被点赞的帖子id、被关注的人的id、被评论的comment的作者的id）
	private int entityUserId; // 事件触发对象（被点赞的人、被关注的人、被私信的人）
	private Map<String, Object> data = new HashMap<>(); // 其他数据

	// 这里是链式编程
	public Event setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	public Event setUserId(int userId) {
		this.userId = userId;
		return this;
	}

	public Event setEntityType(int entityType) {
		this.entityType = entityType;
		return this;
	}

	public Event setEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}

	public Event setEntityUserId(int entityUserId) {
		this.entityUserId = entityUserId;
		return this;
	}

	// 简介定义map
	public Event setData(String key, Object value) {
		this.data.put(key, value);
		return this;
	}

	public String getTopic() {
		return topic;
	}

	public int getUserId() {
		return userId;
	}

	public int getEntityType() {
		return entityType;
	}

	public int getEntityId() {
		return entityId;
	}

	public int getEntityUserId() {
		return entityUserId;
	}

	public Map<String, Object> getData() {
		return data;
	}

}
