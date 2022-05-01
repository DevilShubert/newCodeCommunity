package com.example.newcode.serviceTest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DiscussPostServiceTest {
	@Autowired
	DiscussPostService discussPostService;

	@Test
	public void pageTest() {
		IPage<DiscussPost> iPage = discussPostService.selectMapsPage(0, 1, 2, 0);
		List<DiscussPost> records = iPage.getRecords();
		for (DiscussPost record : records) {
			System.out.println(record);
		}
	}

	@Test
	public void pageCountTest() {
		// 总共多条数据
		System.out.println(discussPostService.selectDiscussPostsByUserID(0));
	}

	@Test
	public void updateCommentCountTest() {
		DiscussPost post = discussPostService.selectDiscussPostsByPostID(284);
		discussPostService.updateCommentCount(post, 1);
	}

	@Test
	public void testQuery() {
		DiscussPost discussPost = discussPostService.selectDiscussPostsByPostID(227);
		System.out.println(discussPost);
	}

	@Test
	public void updateType() {
		DiscussPost post = discussPostService.selectDiscussPostsByPostID(290);
		discussPostService.updateType(post, 0);
	}

	@Test
	public void updateStatus() {
		DiscussPost post = discussPostService.selectDiscussPostsByPostID(290);
		discussPostService.updateStatus(post, 0);
	}
}
