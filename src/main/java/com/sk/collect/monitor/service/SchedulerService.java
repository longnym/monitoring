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

import org.quartz.CronTrigger;
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
import com.sk.collect.monitor.schedule.ScheduledJob;
import com.sk.collect.monitor.vo.Schedule;
import com.sk.collect.monitor.vo.CronResult;

@Service
public class SchedulerService {
	@Autowired
	private ApplicationContext applicationContext;

	@Value("${schedule.search.host}")
	private String searchHost;

	public void scheduleJob(Schedule schd) {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("schdMeta", schd);
		dataMap.put("searchHost", searchHost);

		JobDetail schdJob = newJob(ScheduledJob.class)
				.withIdentity("job_" + schd.getSchdId(), schd.getSchdNm())
				.setJobData(new JobDataMap(dataMap))
				.build();

		Trigger schdTrigger = newTrigger()
				.withIdentity("trigger_" + schd.getSchdId(), schd.getSchdNm())
				.withSchedule(cronSchedule(schd.getCron()))
				.build();

		try {
			sc.scheduleJob(schdJob, schdTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void unscheduleJob(Schedule schd) {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		try {
			sc.unscheduleJob(triggerKey("trigger_" + schd.getSchdId(), schd.getSchdNm()));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void scheduleEsCount() {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		JobDetail countJob = newJob(ElasticsearchCountJob.class)
				.withIdentity("countJob", "elasticsearch")
				.build();

		Trigger countTrigger = newTrigger()
				.withIdentity("cronTrigger", "elasticsearch")
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
			sc.unscheduleJob(triggerKey("cronTrigger", "elasticsearch"));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public List<CronResult> checkScheduler() {
		List<CronResult> results = new ArrayList<CronResult>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");
		try {
			for (String groupName : sc.getJobGroupNames()) {
				for (JobKey jobKey : sc.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					for (Trigger trigger : sc.getTriggersOfJob(jobKey)) {
						CronTrigger ct = (CronTrigger) trigger;
						results.add(new CronResult(groupName, jobKey.getName(), sf.format(ct.getStartTime()), ct.getCronExpression()));
					}
				}
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return results;
	}
}