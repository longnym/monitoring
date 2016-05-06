package com.sk.collect.monitor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sk.collect.monitor.vo.Job;
import com.sk.collect.monitor.vo.Schedule;
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
		for (Job job : jdbcService.searchJobList()) {
			if (job.getStatus().equals("RUNNING")) {
				long jobId = job.getJobId();
				System.out.println("Job " + jobId + " is starting...");

				schedulerService.scheduleJob(job);
				System.out.println("Job " + jobId + " is running.");
			}
		}
	}

	// Job의 스케줄링을 시작
	@RequestMapping("/{jobId}/start")
	public void startJob(@PathVariable("jobId") Long jobId) {
		Job job = jdbcService.searchJob(jobId);

		if (job.getStatus().equals("STOP")) {
			System.out.println("Job " + jobId + " is starting...");

			schedulerService.scheduleJob(job);
			jdbcService.updateStartJob(jobId);
			System.out.println("Job " + jobId + " is running.");
		} else {
			System.out.println("Job " + jobId + " is already running.");
		}
	}

	// Job의 스케줄링을 중지
	@RequestMapping("/{jobId}/stop")
	public void stopJob(@PathVariable("jobId") Long jobId) {
		Job job = jdbcService.searchJob(jobId);

		if (job.getStatus().equals("RUNNING")) {
			System.out.println("Job " + jobId + " is stopping...");

			schedulerService.unscheduleJob(job);
			jdbcService.updateStopJob(jobId);
			System.out.println("Job " + jobId + " is stopped.");
		} else {
			System.out.println("Job " + jobId + " is already stopped.");
		}
	}

	// 스케줄러에 등록된 Job의 목록을 출력
	@RequestMapping("/joblist")
	public @ResponseBody List<Schedule> jobList() {
		return schedulerService.checkScheduler();
	}
}