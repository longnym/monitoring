package com.sk.collect.monitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sk.collect.monitor.vo.Job;
import com.sk.collect.monitor.service.JdbcService;
import com.sk.collect.monitor.service.SchedulerService;

@RestController
@RequestMapping("/schd")
public class SchdRestController {
	@Autowired
	private JdbcService jdbcService;
	
	@Autowired
	private SchedulerService schedulerService;

	@RequestMapping("/init")
	public void initJob() {
		for(Job job : jdbcService.searchJobList()) {
			if (job.getStatus().equals("RUNNING")) {
				long jobId = job.getJobId();
				System.out.println("Job " + jobId + " is starting...");

				schedulerService.scheduleJob(job);
				System.out.println("Job " + jobId + " is running.");
			}
		}
	}

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
}