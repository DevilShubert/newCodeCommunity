package com.example.newcode.service.elasticsearch;

import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.DiscussPostService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class ElasticsearchService {
	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	ElasticsearchOperations elasticsearchOperations;

	public void addDiscussPost(int id) {
		synchronized (elasticsearchOperations) {
			// 查到最新的帖子的
			DiscussPost post = discussPostService.selectDiscussPostsByPostID(id);
			if (post != null) {
				// 按照官网上的案例 Example 65.ElasticsearchOperations usage
				IndexQuery indexQuery = new IndexQueryBuilder().withId(String.valueOf(post.getId())).withObject(
						post).build();
				IndexCoordinates indexCoordinates = IndexCoordinates.of("discusspost");
				elasticsearchOperations.index(indexQuery, indexCoordinates);
			}
		}
	}

	public DiscussPost queryDiscussPostById(int id) {
		// 按照官网上的案例 Example 65.ElasticsearchOperations usage
		IndexCoordinates indexCoordinates = IndexCoordinates.of("discusspost");
		DiscussPost discussPost = elasticsearchOperations.get(String.valueOf(id), DiscussPost.class, indexCoordinates);
		return discussPost;
	}

	public void deleteDiscussPostById(int id) {
		IndexCoordinates indexCoordinates = IndexCoordinates.of("discusspost");
		elasticsearchOperations.delete(String.valueOf(id), indexCoordinates);
	}

	public void updateDiscussPost(DiscussPost newDiscussPost) {
		IndexQuery indexQuery = new IndexQueryBuilder().withId(String.valueOf(newDiscussPost.getId())).withObject(
				newDiscussPost).build();
		IndexCoordinates indexCoordinates = IndexCoordinates.of("discusspost");
		elasticsearchOperations.index(indexQuery, indexCoordinates);
	}

	// 通过关键字查找title 和 content两部分，并带有分页
	public HashMap<List<DiscussPost>, Integer> searchDiscussPostByKeyword(String keyword, int current, int limit) {
		HashMap<List<DiscussPost>, Integer> resHashmap = new HashMap<>();

		// 参考的官网NativeSearchQuery方法（官方有三种CriteriaQuery、StringQuery、NativeSearchQuery）和牛客教程代码
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(
				QueryBuilders.multiMatchQuery(keyword, "title", "content")).withSort(
				SortBuilders.fieldSort("type").order(SortOrder.DESC)).withSort(
				SortBuilders.fieldSort("score").order(SortOrder.DESC)).withPageable(PageRequest.of(current - 1, limit))
				// 分页查找
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC)).withHighlightFields(
						// title字段中检索出来的关键字高亮
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")).build();
		IndexCoordinates indexCoordinates = IndexCoordinates.of("discusspost");
		SearchHits<DiscussPost> search = elasticsearchOperations.search(searchQuery, DiscussPost.class,
				indexCoordinates);

		List<DiscussPost> list = new ArrayList<>();
		List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();

		for (SearchHit<DiscussPost> searchHit : searchHits) {
			DiscussPost post = new DiscussPost();
			post.setId(searchHit.getContent().getId());
			post.setUserId(searchHit.getContent().getUserId());
			post.setTitle(searchHit.getContent().getTitle());
			post.setContent(searchHit.getContent().getContent());
			post.setStatus(searchHit.getContent().getStatus());
			post.setCreateTime(searchHit.getContent().getCreateTime());
			post.setCommentCount(searchHit.getContent().getCommentCount());

			// 处理高亮
			List<String> title = searchHit.getHighlightFields().get("title");
			if (title != null) {
				// 虽然是用list存储，但是都被高亮，元素值只有一个
				post.setTitle(title.get(0));
			}
			List<String> content = searchHit.getHighlightFields().get("content");
			if (content != null) {
				// 虽然是用list存储，但是都被高亮，元素值只有一个
				post.setContent(content.get(0));
			}
			list.add(post);
		}
		// 第一个参数为当前页的帖子的集合，第二个参数为总共hits中了多少条帖子
		resHashmap.put(list, Integer.valueOf(String.valueOf(search.getTotalHits())));

		return resHashmap;
	}
}
