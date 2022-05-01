package com.example.newcode.config;

import com.example.newcode.quartz.job.AlphaJob;
import com.example.newcode.quartz.job.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {
	// 配置JobDetail
	// @Bean
	public JobDetailFactoryBean alphaJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(AlphaJob.class);
		factoryBean.setName("alphaJob"); // job名称
		factoryBean.setGroup("alphaJobGroup"); // job所属于哪一组
		factoryBean.setDurability(true); // 是否是可复用的，也就是说Trigger消失后job是否还存在
		factoryBean.setRequestsRecovery(true); // 项目重启后job是否可恢复
		return factoryBean;
	}

	// 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
	// @Bean
	public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(alphaJobDetail);
		factoryBean.setName("alphaTrigger");
		factoryBean.setGroup("alphaTriggerGroup");
		factoryBean.setRepeatInterval(3000);
		factoryBean.setJobDataMap(new JobDataMap());
		return factoryBean;
	}

	// 刷新帖子分数任务
	@Bean
	public JobDetailFactoryBean postScoreRefreshJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(PostScoreRefreshJob.class);
		factoryBean.setName("postScoreRefreshJob");
		factoryBean.setGroup("communityJobGroup");
		factoryBean.setDurability(true);
		factoryBean.setRequestsRecovery(true);
		return factoryBean;
	}

	@Bean
	public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(postScoreRefreshJobDetail);
		factoryBean.setName("postScoreRefreshTrigger");
		factoryBean.setGroup("communityTriggerGroup");
		factoryBean.setRepeatInterval(1000 * 60 * 5); // 5min 刷新一次
		factoryBean.setJobDataMap(new JobDataMap());
		return factoryBean;
	}
}
