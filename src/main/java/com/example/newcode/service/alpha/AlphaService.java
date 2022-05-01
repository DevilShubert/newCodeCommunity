package com.example.newcode.service.alpha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlphaService {
	// 让该方法在多线程环境下,被异步的调用.
	// @Async
	public void execute1() {
		log.info("对于Spring普通线程池(简化)的测试");
	}

	// @Scheduled(initialDelay = 2000, fixedRate = 500)
	public void execute2() {
		log.info("对于Spring定时任务线程池(简化)的测试");
	}
}
