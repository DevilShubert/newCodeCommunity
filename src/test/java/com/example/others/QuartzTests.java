package com.example.others;

import com.example.newcode.NewCodeApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = NewCodeApplication.class)
public class QuartzTests {
	@Autowired
	private Scheduler scheduler;

	@Test
	public void testDeleteJob() {
		try {
			// job名 + 组名
			boolean result = scheduler.deleteJob(new JobKey("postScoreRefreshJob", "communityJobGroup"));
			// boolean result = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
			System.out.println(result);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
