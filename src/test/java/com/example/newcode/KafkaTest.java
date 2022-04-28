package com.example.newcode;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@SpringBootTest
@Slf4j
public class KafkaTest {
	@Autowired
	private KafkaProducer kafkaProducer;

	@Test
	public void testKafka() {
		kafkaProducer.sendMessage("test", "tiantiantintia");
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

@Component
class KafkaProducer {

	@Autowired
	private KafkaTemplate kafkaTemplate;

	public void sendMessage(String topic, String content) {
		kafkaTemplate.send(topic, content);
	}
}

@Component
class KafkaConsumer {

	@KafkaListener(topics = { "test" })
	public void handleMessage(ConsumerRecord record) {
		System.out.println(record.value());
	}
}

