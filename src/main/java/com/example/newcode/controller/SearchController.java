package com.example.newcode.controller;

import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.MyPage;
import com.example.newcode.service.LikeService;
import com.example.newcode.service.UserService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private UserService userService;

	@Autowired
	private LikeService likeService;

	/**
	 * 通过关键字实行Elasticsearch搜索
	 *
	 * @param keyword
	 * @param page
	 * @param model
	 * @return
	 */
	// search?keyword=xxx
	@RequestMapping(path = "/search", method = RequestMethod.GET)
	public String search(String keyword, MyPage page, Model model) {
		// 按照page对象分页查询，如果是第一次访问则MVC自动实例化page对象，否则尝试从前端接收
		HashMap<List<DiscussPost>, Integer> resHashmap = elasticsearchService.searchDiscussPostByKeyword(keyword,
				page.getCurrent(), page.getLimit());
		List<DiscussPost> searchResult = resHashmap.keySet().iterator().next();

		// 搜索到的总记录数
		Integer totalHits = resHashmap.get(searchResult);
		page.setRows(totalHits);

		// 设置总页数
		if (totalHits > 0) {
			int pages =
					totalHits % page.getLimit() == 0 ? totalHits / page.getLimit() : totalHits / page.getLimit() + 1;
			page.setTotal(pages);
		} else {
			page.setTotal(0);
		}

		// 设置访问路径
		page.setPath("/search?keyword=" + keyword);

		// 聚合数据
		List<Map<String, Object>> discussPosts = new ArrayList<>();
		if (searchResult != null) {
			for (DiscussPost post : searchResult) {
				Map<String, Object> map = new HashMap<>();
				// 帖子
				map.put("post", post);
				// 作者
				map.put("user", userService.selectById(post.getUserId()));
				// 点赞数量
				map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TARGET_POST, post.getId()));
				discussPosts.add(map);
			}
		}
		model.addAttribute("discussPosts", discussPosts);
		model.addAttribute("keyword", keyword);
		model.addAttribute("page", page);
		return "/site/search";
	}

}
