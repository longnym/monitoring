package com.sk.collect.monitor.service;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.sk.collect.monitor.schedule.ElasticsearchCountJob;
import com.sk.collect.monitor.schedule.ScheduledSearchJob;
import com.sk.collect.monitor.vo.Job;
import com.sk.collect.monitor.vo.Schedule;

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
		dataMap.put("searchHost", searchHost);
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

	public void scheduleEsCount() {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		JobDetail countJob = newJob(ElasticsearchCountJob.class).withIdentity("countJob", "es").build();

		Trigger countTrigger = newTrigger()
				.withIdentity("cronTrigger", "es")
				.withSchedule(cronSchedule("0/1 * * * * ?"))
				.build();

		try {
			sc.scheduleJob(countJob, countTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void unscheduleEsCount() {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");
		try {
			sc.unscheduleJob(triggerKey("cronTrigger", "es"));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public List<Schedule> checkScheduler() {
		List<Schedule> schdList = new ArrayList<Schedule>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");
		try {
			for (String groupName : sc.getJobGroupNames()) {
				for (JobKey jobKey : sc.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					for (Trigger trigger : sc.getTriggersOfJob(jobKey)) {
						schdList.add(new Schedule(groupName, jobKey.getName(), sf.format(trigger.getStartTime())));
					}
				}
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return schdList;
	}
}