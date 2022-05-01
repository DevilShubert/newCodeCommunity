package com.example.others;

import com.example.newcode.NewCodeApplication;
import com.example.newcode.dao.DiscussPostDao;
import com.example.newcode.entity.DiscussPost;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest(classes = NewCodeApplication.class)
public class JMeterTest {

	@Autowired
	DiscussPostDao discussPostDao;

	@Test
	public void insertPost(){
		// 插入30w条数据
		for (int i = 0; i < 300000; i++) {
			DiscussPost post = new DiscussPost();
			post.setUserId(111);
			post.setTitle("JMeter测试");
			post.setContent("JMeter测试JMeter测试JMeter测试JMeter测试JMeter测试JMeter测试");
			post.setCreateTime(new Date());
			post.setScore(Math.random() * 2000);
			discussPostDao.insert(post);
		}
	}
}
