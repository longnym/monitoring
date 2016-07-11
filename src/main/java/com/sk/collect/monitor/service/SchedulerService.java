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
import org.quartz.ObjectAlreadyExistsException;
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
	
	@Value("${schedule.monitor.interval}")
	private String monInterval;

	// 스케줄을 실행
	public int scheduleJob(Schedule schd) {
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
		} catch (ObjectAlreadyExistsException e) {
			return -2;
		}
		catch (SchedulerException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	// 스케줄을 종료
	public void unscheduleJob(Schedule schd) {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		try {
			sc.unscheduleJob(triggerKey("trigger_" + schd.getSchdId(), schd.getSchdNm()));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	// Elasticsearch의 index Count를 집계하는 스케줄러를 구동함
	public void scheduleEsCount(String index, String[] type) {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("index", index);
		dataMap.put("type", type);

		JobDetail countJob = newJob(ElasticsearchCountJob.class)
				.withIdentity("countJob", "elasticsearch")
				.setJobData(new JobDataMap(dataMap))
				.build();

		Trigger countTrigger = newTrigger()
				.withIdentity("cronTrigger", "elasticsearch")
				.withSchedule(cronSchedule("0/" + monInterval + " * * * * ?"))
				.build();
		try {
			sc.scheduleJob(countJob, countTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	// Elasticsearch의 index Count를 집계하는 스케줄러를 중단함
	public void unscheduleEsCount() {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");
		try {
			sc.unscheduleJob(triggerKey("cronTrigger", "elasticsearch"));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	// 현재 수행중인 스케줄러의 목록을 출력
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