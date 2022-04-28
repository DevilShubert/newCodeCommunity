package com.example.newcode.event;

import com.alibaba.fastjson.JSONObject;
import com.example.newcode.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
	@Autowired
	private KafkaTemplate kafkaTemplate;

	// 处理时间
	public void fireEvent(Event event) {
		// 将事件发布到指定的主题：使用fastjson转换为json格式
		kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
	}
}
