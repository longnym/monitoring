package com.sk.collect.monitor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sk.collect.monitor.vo.Schedule;
import com.sk.collect.monitor.vo.CronResult;
import com.sk.collect.monitor.service.JdbcService;
import com.sk.collect.monitor.service.SchedulerService;

@RestController
@RequestMapping("/schd")
public class SchdRestController {
	@Autowired
	private JdbcService jdbcService;

	@Autowired
	private SchedulerService schedulerService;

	// 스케줄링을 초기화. 이전상태가 실행중이면 재 시작
	@RequestMapping("/init")
	public void initJob() {
		for (Schedule schd : jdbcService.searchScheduleList()) {
			long schdId = schd.getSchdId();

			System.out.println("Job " + schdId + " is starting...");
			schedulerService.scheduleJob(schd);
			System.out.println("Job " + schdId + " is running.");
		}
	}

	// Job의 스케줄링을 시작
	@RequestMapping("/{schdId}/start")
	public void startJob(@PathVariable("schdId") Long schdId) {
		Schedule schd = jdbcService.searchSchedule(schdId);

		System.out.println("Job " + schdId + " is starting...");
		schedulerService.scheduleJob(schd);
		System.out.println("Job " + schdId + " is running.");
	}

	// Job의 스케줄링을 중지
	@RequestMapping("/{schdId}/stop")
	public void stopJob(@PathVariable("schdId") Long schdId) {
		Schedule schd = jdbcService.searchSchedule(schdId);

		System.out.println("Job " + schdId + " is stopping...");
		schedulerService.unscheduleJob(schd);
		System.out.println("Job " + schdId + " is stopped.");
	}

	// 스케줄러에 등록된 Job의 목록을 출력
	@RequestMapping("/joblist")
	public @ResponseBody List<CronResult> jobList() {
		return schedulerService.checkScheduler();
	}
}