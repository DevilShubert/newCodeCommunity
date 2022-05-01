package com.example.others;

import com.example.newcode.NewCodeApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@Slf4j
@SpringBootTest(classes = NewCodeApplication.class)
public class WkTests {

	@Value("${wk.image.storage}")
	private String wkImageStorage;


	@Value("${community.epoch}")
	private String date;

	@Test
	public void storeImageTest() {
		String cmd = "/usr/local/bin/wkhtmltoimage  "
				+ "https://www.baidu.com "
				+ "/Users/liuzheran/Desktop/学习笔记/后端部分/springBoot/项目/NewCode/newCodeWKImages/1.png";
		try {
			Process exec = Runtime.getRuntime().exec(cmd);
			log.info("开始接收");
			if (exec.waitFor() == 0) {
				log.info("接收完毕");
			}
			System.out.println("ok.");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void valueTest() {
		System.out.println(wkImageStorage);
		System.out.printf(date);
	}




}
