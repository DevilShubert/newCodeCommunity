package com.example.newcode.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.impl.DiscussPostServiceImpl;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class CaffeineConfig {
	@Value("${caffeine.posts.max-size}")
	private int maxSize;

	@Value("${caffeine.posts.expire-seconds}")
	private int expireSeconds;

	@Autowired
	DiscussPostServiceImpl discussPostService;

	@PostConstruct
	public void init() {
		initLoadingCache(); // 用于想容器注入核心组件
	}

	// 热门帖子缓存
	@Bean
	public LoadingCache<String, IPage<DiscussPost>> initLoadingCache(){
		// 初始化帖子列表缓存
		LoadingCache<String, IPage<DiscussPost>> listLoadingCache = Caffeine.newBuilder().
				maximumSize(maxSize).
				expireAfterWrite(
					expireSeconds, TimeUnit.SECONDS).
				build(new CacheLoader<String, IPage<DiscussPost>>() {
			// 这个匿名接口的作用是当缓存中没有是如何去数据库中查找
			@Nullable
			@Override
			public IPage<DiscussPost> load(@NonNull String key) throws Exception {
				if (key == null || key.length() == 0) {
					throw new IllegalArgumentException("参数错误!");
				}

				String[] params = key.split(":");
				if (params == null || params.length != 2) {
					throw new IllegalArgumentException("参数错误!");
				}

				int page = Integer.valueOf(params[0]);
				int pageSize = Integer.valueOf(params[1]);

				// 二级缓存: Redis -> mysql（如果要使用）

				log.info("load post list from DB.");
				IPage<DiscussPost> postIPage = discussPostService.selectMapsPage(0, page, pageSize, 1);
				return postIPage;
			}
		});
		return listLoadingCache;
	}

}
