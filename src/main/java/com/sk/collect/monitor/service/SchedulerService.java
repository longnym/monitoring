package com.sk.collect.monitor.service;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.util.HashMap;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.sk.collect.monitor.schedule.ScheduledSearchJob;
import com.sk.collect.monitor.vo.Job;

@Service
public class SchedulerService {
	@Autowired
	private ApplicationContext applicationContext;

	@Value("${schedule.search.host}")
	private String searchHost;

	@Value("${schedule.save.index}")
	private String saveIndex;

	@Value("${schedule.save.type}")
	private String saveType;

	public void scheduleJob(Job job) {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("jobMeta", job);
		dataMap.put("searchUrl", searchHost);
		dataMap.put("saveIndex", saveIndex);
		dataMap.put("saveType", saveType);

		JobDetail schdJob = newJob(ScheduledSearchJob.class)
				.withIdentity("job_" + job.getJobId(), "schedule")
				.setJobData(new JobDataMap(dataMap))
				.build();

		Trigger schdTrigger = newTrigger()
				.withIdentity("trigger_" + job.getJobId(), "schedule")
				.withSchedule(cronSchedule(job.getSchedule()))
				.build();

		try {
			sc.scheduleJob(schdJob, schdTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void unscheduleJob(Job job) {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		try {
			sc.unscheduleJob(triggerKey("trigger_" + job.getJobId(), "schedule"));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}