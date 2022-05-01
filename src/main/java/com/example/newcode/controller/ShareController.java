package com.example.newcode.controller;

import com.example.newcode.entity.Event;
import com.example.newcode.event.EventProducer;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.constant.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class ShareController implements CommunityConstant {

	@Autowired
	private  EventProducer eventProducer;

	@Value("${community.path.domain}")
	private  String domain;

	@Value("${server.servlet.context-path}")
	private  String contextPath;

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@Value("${qiniu.bucket.share.url}") // share空间的域名
	private String shareBucketUrl;

	@RequestMapping(path = "/share/{htmlUrl}", method = RequestMethod.GET)
	@ResponseBody
	public String share(@PathVariable("htmlUrl") String plainHtmlUrl) {
		// 文件名
		String fileName = CommunityUtils.getRandomUUID();

		String htmlUrl = "https://" + plainHtmlUrl + "/";

		// 异步生成长图
		Event event = new Event()
				.setTopic(TOPIC_SHARE)
				.setData("htmlUrl", htmlUrl)
				.setData("fileName", fileName)
				.setData("suffix", ".png");
		eventProducer.fireEvent(event);

		// 返回用户访问路径
		Map<String, Object> map = new HashMap<>();
		// String  shareUrl = domain + contextPath + "/share/image/" + fileName;
		String shareUrl = shareBucketUrl + "/" + fileName;
		map.put("shareUrl", shareUrl);

		// 直接返回一个String字符串
		return CommunityUtils.getJSONString(0, null, map);
	}



	// 废弃
	// 获取长图
	@RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
	public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("文件名不能为空!");
		}

		// 设置响应为图片格式才能传输
		response.setContentType("image/png");
		File file = new File(wkImageStorage + "/" + fileName + ".png");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int b = 0;
			while ((b = fis.read(buffer)) != -1) {
				os.write(buffer, 0, b);
			}
		} catch (IOException e) {
			log.error("获取长图失败: " + e.getMessage());
		}
	}

}
