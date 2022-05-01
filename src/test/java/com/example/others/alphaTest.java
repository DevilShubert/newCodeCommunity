package com.example.others;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.NewCodeApplication;
import com.example.newcode.dao.DiscussPostDao;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.LikeService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
import com.example.newcode.util.constant.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Slf4j
@SpringBootTest(classes = NewCodeApplication.class)
public class alphaTest implements CommunityConstant {

	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	ElasticsearchService elasticsearchService;

	@Autowired
	DiscussPostDao discussPostDao;

	@Autowired
	LikeService likeService;

	private static Date epoch;

	static {
		try {
			epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void calAllScore() {
		// 查询出所有帖子
		Page<DiscussPost> postPage = new Page<>(1, Integer.MAX_VALUE);
		QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
		List<DiscussPost> posts = discussPostDao.selectPage(postPage, wrapper).getRecords();

		// 将所有帖子都计算出score并存入到MySQL和ES中
		Iterator<DiscussPost> iterator = posts.iterator();
		while (iterator.hasNext()) {
			DiscussPost post = iterator.next();
			refresh(post.getId());
		}
	}

	// 根据postId更新帖子对应的分数
	private void refresh(Integer postId) {
		QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
		wrapper.eq("id", postId);
		DiscussPost post = discussPostDao.selectOne(wrapper);

		if (post == null) {
			// 注意这里有可能是帖子在经历加精、帖子回帖、对帖子点赞或取消赞之后又被管理员或者user删除
			// 这个时候还是计算出帖子的最新的score，因为万一之后帖子又被恢复呢？
			log.info("该帖子不存在: id = " + postId);
			return;
		}

		// 是否精华
		boolean wonderful = post.getStatus() == 1;
		// 评论数量
		int commentCount = post.getCommentCount();
		// 点赞数量
		long likeCount = likeService.findEntityLikeCount(ENTITY_TARGET_POST, postId);

		// 计算权重w1
		double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
		double score1 = Math.log10(Math.max(w, 1)); // 防止w < 1而导致对数为负数的情况
		double score2 = (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
		double totalScore = score1 + score2;

		// 更新MySQL数据库中的帖子的score
		discussPostService.updateScore(post, totalScore);

		// 更新ES中帖子的score
		post.setScore(totalScore);
		elasticsearchService.updateDiscussPost(post);
	}
}
