package com.sk.collect.monitor.vo;

public class CronResult {
	private final String group;
	private final String job;
	private final String start;
	private final String cron;

	public CronResult(String group, String job, String start, String cron) {
		this.group = group;
		this.job = job;
		this.start = start;
		this.cron = cron;
	}

	public String getGroup() {
		return group;
	}

	public String getJob() {
		return job;
	}

	public String getStart() {
		return start;
	}

	public String getCron() {
		return cron;
	}
}