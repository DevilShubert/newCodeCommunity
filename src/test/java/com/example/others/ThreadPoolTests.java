package com.example.others;

import com.example.newcode.NewCodeApplication;
import com.example.newcode.service.alpha.AlphaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;
import java.util.concurrent.*;

@Slf4j
@SpringBootTest(classes = NewCodeApplication.class)
public class ThreadPoolTests {

	// JDK普通线程池
	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	// JDK可执行定时任务的线程池
	// 为什么要线程池池？假如只有一个线程负责定时任务，上一个任务没有执行完则肯定要阻塞
	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

	// Spring普通线程池
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	// Spring可执行定时任务的线程池
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	AlphaService alphaService;

	// sleep方法
	private void sleep(long m) {
		try {
			Thread.sleep(m);
			// 使用catch就不会将异常向上抛出
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 1.JDK普通线程池
	@Test
	public void testExecutorService() {
		for (int i = 0; i < 10; i++) {
			executorService.submit(() -> {
				log.info("对于JDK普通线程池的测试");
			});
		}
		sleep(2000);
	}

	// 2.JDK定时任务线程池
	@Test
	public void testScheduledExecutorService() {
		Runnable task = () -> {
			sleep(1000 * 3);
			log.info("对于JDK定时任务线程池的测试");
		};

		// 参数：任务、定时任务开始的延迟时间、每次任务的间隔时间、时间单位
		// Future封装了任务的状态，并且可以用Future停止定时器
		Future future = scheduledExecutorService.scheduleAtFixedRate(task, 3000, 500, TimeUnit.MILLISECONDS);

		future.cancel(true);

		sleep(1000 * 5);
	}

	// 3.Spring普通线程池
	@Test
	public void testThreadPoolTaskExecutor() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				log.info("对于Spring普通线程池的测试");
			}
		};

		for (int i = 0; i < 10; i++) {
			taskExecutor.submit(task);
		}
		sleep(5000);
	}

	// 4.Spring定时任务线程池
	@Test
	public void testThreadPoolTaskScheduler() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				log.info("对于Spring定时任务线程池的测试");
			}
		};
		// 手动设置延迟时间
		Date startTime = new Date(System.currentTimeMillis() + 10000);
		taskScheduler.scheduleAtFixedRate(task, startTime, 1000);

		sleep(30000);
	}

	// 5.Spring普通线程池(简化)
	@Test
	public void testThreadPoolTaskExecutorSimple() {
		for (int i = 0; i < 10; i++) {
			alphaService.execute1(); // 在调用时Spring会使用线程池自动，且多线程地调用该方法
		}
		sleep(3000);
	}

	// 6.Spring定时任务线程池(简化)
	@Test
	public void testThreadPoolTaskSchedulerSimple() {
		sleep(5000);
	}
}
