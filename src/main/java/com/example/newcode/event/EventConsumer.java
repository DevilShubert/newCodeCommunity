package com.example.newcode.event;

import com.alibaba.fastjson.JSONObject;
import com.example.newcode.entity.Event;
import com.example.newcode.entity.Message;
import com.example.newcode.service.MessageService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
import com.example.newcode.util.constant.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class EventConsumer implements CommunityConstant {

	@Autowired
	private MessageService messageService;

	@Autowired
	ElasticsearchService elasticsearchService;

	// 消费者自动监听消息：回帖与回复、点赞、关注
	@KafkaListener(topics = { TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW })
	public void handleCommentMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空!");
			return;
		}

		// 解析消息
		Event event = JSONObject.parseObject(record.value().toString(), Event.class);

		// 发送站内通知
		Message message = new Message();
		message.setFromId(SYSTEM_USER_ID);
		message.setToId(event.getEntityUserId());
		message.setConversationId(event.getTopic());
		message.setCreateTime(new Date());

		// 设置message中的content基本内容
		Map<String, Object> content = new HashMap<>();
		content.put("userId", event.getUserId()); // 事件发出者
		content.put("entityType", event.getEntityType()); // 事件类型
		content.put("entityId", event.getEntityId());

		// 除此之外存入event的其他内容
		if (!event.getData().isEmpty()) {
			for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
				content.put(entry.getKey(), entry.getValue());
			}
		}

		message.setContent(JSONObject.toJSONString(content));
		messageService.insertMessage(message);
	}

	// 消费者自动监听消息：发布帖子或回帖时，ES中的数据都会更新
	@KafkaListener(topics = { TOPIC_PUBLISH })
	public void handlePublishMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误!");
			return;
		}
		// 可能是新加入的帖子，也可能是帖子有新的回帖之后更新
		elasticsearchService.addDiscussPost(event.getEntityId());
	}

	// 删帖的消费者事件
	@KafkaListener(topics = { TOPIC_DELETE })
	public void handleDeleteMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空!");
			return;
		}
		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误!");
			return;
		}
		elasticsearchService.deleteDiscussPostById(event.getEntityId());
	}

}
