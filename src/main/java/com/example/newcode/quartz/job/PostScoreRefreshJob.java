package com.example.newcode.quartz.job;

import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.LikeService;
import com.example.newcode.service.elasticsearch.ElasticsearchService;
import com.example.newcode.util.RedisUtils;
import com.example.newcode.util.constant.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class PostScoreRefreshJob implements Job, CommunityConstant {

	@Autowired
	RedisUtils redisUtils;

	@Autowired
	DiscussPostService discussPostService;

	@Autowired
	LikeService likeService;

	@Autowired
	ElasticsearchService elasticsearchService;

	// 定义epoch讨论区的起始纪元
	private static Date epoch;

	static {
		try {
			epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		String scoreKey = RedisUtils.getPostScoreKey();
		long size = redisUtils.sGetSetSize(scoreKey);
		if (size == 0) {
			log.info("[任务取消] 没有需要刷新的帖子!");
			return;
		}
		log.info("[任务开始] 正在刷新帖子分数: ");
		while (redisUtils.sGetSetSize(scoreKey) > 0) {
			Integer postId = (Integer) redisUtils.sRandomGet(scoreKey);
			log.info("[任务执行中] 正在刷新第：" + postId + "的分数");
			refresh(postId);
		}
		log.info("[任务结束] 帖子分数刷新完毕!");
	}

	// 根据postId更新帖子对应的分数
	private void refresh(Integer postId) {
		DiscussPost post = discussPostService.selectDiscussPostsByPostID(postId);

		if (post == null) {
			// 注意这里有可能是帖子在经历加精、帖子回帖、对帖子点赞或取消赞之后又被管理员或者user删除
			// 这个时候还是计算出帖子的最新的score，因为万一之后帖子又被恢复呢？
			log.info("该帖子不存在: id = " + postId);
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
