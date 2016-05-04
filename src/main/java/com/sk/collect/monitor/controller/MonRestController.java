package com.sk.collect.monitor.controller;

import java.text.SimpleDateFormat;
import java.util.List;

import com.sk.collect.monitor.vo.Count;
import com.sk.collect.monitor.service.ElasticsearchService;
import com.sk.collect.monitor.schedule.ElasticsearchCountJob;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.quartz.JobKey;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.SchedulerException;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;

@RestController
public class MonRestController {
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ElasticsearchService elasticsearchService;

	// elasticsearch에 카운트를 저장
	@RequestMapping(value = "/mon/putCount", method = RequestMethod.POST)
	public void putCount(@RequestBody String message) {
		elasticsearchService.indexCount(message);
	}

	// elasticsearch의 카운트 데이터를 삭제
	@RequestMapping("/mon/delCount")
	public void delCount(
			@RequestParam(value = "date_from", defaultValue = "") String date_from,
			@RequestParam(value = "date_to", defaultValue = "") String date_to) {
		elasticsearchService.deleteCount(date_from, date_to);
	}

	// elasticsearch의 카운트를 조회
	@RequestMapping("/mon/getCount")
	public @ResponseBody List<Count> getCount(
			@RequestParam(value = "date_from", defaultValue = "") String date_from,
			@RequestParam(value = "date_to", defaultValue = "") String date_to,
			@RequestParam(value = "type", defaultValue = "") String type,
			@RequestParam(value = "host", defaultValue = "") String host,
			@RequestParam(value = "source", defaultValue = "") String source) {
		return elasticsearchService.searchCount(date_from, date_to, type, host);
	}

	// elasticsearch의 저장 건수를 계산하여 저장하는 스케줄러 구동
	@RequestMapping("/mon/esCount/start")
	public void startEsCount() throws JsonProcessingException, SchedulerException {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		JobDetail countJob = newJob(ElasticsearchCountJob.class)
				.withIdentity("countJob", "es")
				.build();

		Trigger countTrigger = newTrigger()
				.withIdentity("cronTrigger", "es")
				.withSchedule(cronSchedule("0/1 * * * * ?"))
				.build();

		sc.scheduleJob(countJob, countTrigger);
	}

	// elasticsearch의 저장 건수를 계산하여 저장하는 스케줄러 상태 확인
	@RequestMapping("/mon/esCount/state")
	public void stateEsCount() throws JsonProcessingException, SchedulerException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");

		for(String groupName : sc.getJobGroupNames()) {
			for (JobKey jobKey : sc.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
				System.out.println("job name: " + jobKey.getName());
				for(Trigger trigger : sc.getTriggersOfJob(jobKey)) {
					String time = sf.format(trigger.getStartTime());
					System.out.println("excution start time: " + time);
				}
			}
		}
	}

	// elasticsearch의 저장 건수를 계산하여 저장하는 스케줄러 중지
	@RequestMapping("/mon/esCount/stop")
	public void stopEsCount() throws JsonProcessingException, SchedulerException {
		StdScheduler sc = (StdScheduler) applicationContext.getBean("schedulerFactoryBean");
		sc.unscheduleJob(triggerKey("cronTrigger", "es"));
		System.out.println("Schedule is stopped.");
	}
}