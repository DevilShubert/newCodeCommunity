package com.example.newcode.elasticsearch;

import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;

@SpringBootTest
@Slf4j
public class EsTest {
	// 对于ES索引的增删改查

	@Autowired
	RestHighLevelClient highLevelClient; // Elasticsearch Client
	// 在当前版本中直接使用client的方式很多都被废弃了，推荐混合使用下面这两个

	@Autowired
	ElasticsearchService elasticsearchService;    // Elasticsearch Repositories

	@Autowired
	DiscussPostService discussPostService;

	@Test
	public void testAdd() {
		// 增加文档
		elasticsearchService.addDiscussPost(287);
	}

	@Test
	public void testQuery() {
		// 查询文档
		DiscussPost discussPost = elasticsearchService.queryDiscussPostById(285);
		System.out.println(discussPost);
	}

	@Test
	public void deleteTest() {
		// 删除文档
		elasticsearchService.deleteDiscussPostById(288);
	}

	@Test
	public void updateTest() {
		// 更新文档
		DiscussPost post = elasticsearchService.queryDiscussPostById(285);
		post.setContent("123123123");
		elasticsearchService.updateDiscussPost(post);
	}

	@Test
	public void searchTest() {
		HashMap<List<DiscussPost>, Integer> map = elasticsearchService.searchDiscussPostByKeyword("调试", 0, 2);
		for (List<DiscussPost> list : map.keySet()) {
			for (DiscussPost post : list) {
				System.out.println(post);
			}
		}
	}

	@Test
	public void addAllPost() {
		for (int i = 109; i <= 288; i++) {
			elasticsearchService.addDiscussPost(i);
		}
	}
}
