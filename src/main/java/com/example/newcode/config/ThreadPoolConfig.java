package com.example.newcode.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // 启用 Spring 的定时任务执行能力
@EnableAsync // 启用 Spring 的异步方法执行能力
public class ThreadPoolConfig {
}
