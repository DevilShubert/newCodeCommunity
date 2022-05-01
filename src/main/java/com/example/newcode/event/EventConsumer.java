package com.example.newcode.event;

import com.alibaba.fastjson.JSONObject;
import com.example.newcode.entity.Event;
import com.example.newcode.entity.Message;
import com.example.newcode.service.MessageService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.constant.CommunityConstant;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.RegionGroup;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
@Slf4j
public class EventConsumer implements CommunityConstant {

	@Autowired
	private MessageService messageService;

	@Autowired
	ElasticsearchService elasticsearchService;

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@Value("${wk.image.command}")
	private  String wkImageCommand;


	@Value("${qiniu.key.access}")
	String accessKey;

	@Value("${qiniu.key.secret}")
	String secretKey;

	@Value("${qiniu.bucket.share.name}") // share的空间名
	private String shareBucketName;

	@Value("${qiniu.bucket.share.url}") // share空间的域名
	private String shareBucketUrl;


	// springboot自带的定时任务新线程，在yaml中已经进行了配置
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	// Spring普通线程池
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;


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

	// 生成长图
	@KafkaListener(topics = { TOPIC_SHARE })
	public void handleShareMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			log.error("消息的内容为空!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			log.error("消息格式错误!");
			return;
		}

		// 解析
		String htmlUrl = (String) event.getData().get("htmlUrl");
		String fileName = (String) event.getData().get("fileName");
		String suffix = (String) event.getData().get("suffix");

		// 生成对应指令
		String cmd = wkImageCommand + " --quality 75 "
				+ htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;

		// 生成长图任务
		Runnable execTask = new Runnable() {
			@Override
			public void run() {
				try {
					// 使用wk需要等待一段时间，拿到Process线程的抽象类
					Process exec = Runtime.getRuntime().exec(cmd);

					// 这样会阻塞Kafka消费者线程直到exec结束
					if (exec.waitFor() == 0) {
						log.info("生成长图成功: " + cmd);
					} else {
						log.error("生成长图失败，图片下载异常");
					}
				} catch (IOException | InterruptedException e) {
					log.error("生成长图失败: " + e.getMessage());
					e.printStackTrace();
				}
			}
		};

		// 使用springboot的线程池去执行生成长图的进程
		taskExecutor.submit(execTask);
		// new Thread(execTask).start();

		// 再使用定时任务去检查长图是否生成，一旦生成了,则上传至七牛云.
		UploadTask task = new UploadTask(fileName, suffix);
		// 返回的Future封装了任务的状态，并且可以用Future停止定时器
		// 重点：任务与任务间会阻塞，例如每次间隔时间为500ms，但如果每次任务自己就会阻塞1000ms，那相当于间隔时间变为1000ms
		Future future = taskScheduler.scheduleAtFixedRate(task, 500);
		// 多线程操作共享变量，会发生同步问题，例如下面强制造成future空指针异常
		// Thread.sleep(30 * 1000);
		task.setFuture(future);
	}

	// 这个UploadTask对象在多次定时任务中是一个对象只不过包装他的是多个ScheduledFutureTask对象，其中time和序号不同
	class UploadTask implements Runnable {

		// 文件名称
		private String fileName;
		// 文件后缀
		private String suffix;
		// 启动任务的返回值
		private Future future;
		// 开始时间
		private long startTime;
		// 上传次数
		private int uploadTimes;

		public UploadTask(String fileName, String suffix) {
			this.fileName = fileName;
			this.suffix = suffix;
			this.startTime = System.currentTimeMillis();
		}

		public void setFuture(Future future) {
			this.future = future;
		}

		@Override
		public void run() {
			// 生成失败
			if (System.currentTimeMillis() - startTime > 30000) {
				log.error("执行时间过长,终止任务:" + fileName);
				future.cancel(true);
				return;
			}
			// 上传失败
			if (uploadTimes >= 3) {
				log.error("上传次数过多,终止任务:" + fileName);
				future.cancel(true);
				return;
			}

			// 找到本地的长图
			String path = wkImageStorage + "/" + fileName + suffix;
			File file = new File(path);
			if (file.exists()) {
				log.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
				// 设置期望的响应信息
				StringMap policy = new StringMap();
				policy.put("returnBody", CommunityUtils.getJSONString(0));
				// 生成上传凭证
				Auth auth = Auth.create(accessKey, secretKey);
				String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
				// 指定上传机房
				Region region2 = Region.region2(); // 我的是华南机房
				RegionGroup regionGroup = new RegionGroup();
				regionGroup.addRegion(region2);
				Configuration config = new Configuration(regionGroup);
				UploadManager manager = new UploadManager(config);
				try {
					// 开始上传图片，这里会阻塞，但是不影响定时器任务的执行，每次任务都会保证先执行完当前任务
					// 就算阻塞实现超过了定时任务的时间间隔，则依然会阻塞，直到本次任务执行完
					Response response = manager.put(
							path, fileName, uploadToken, null, "image/" + suffix, false);
					// 处理响应结果，这里可能会失败，失败则进入下一次上传尝试
					JSONObject json = JSONObject.parseObject(response.bodyString());
					if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
						log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
					} else {
						log.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
						future.cancel(true);
					}
				} catch (QiniuException e) {
					log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
				}
			} else {
				log.info("等待图片生成[" + fileName + "].");
			}
		}
	}
}
