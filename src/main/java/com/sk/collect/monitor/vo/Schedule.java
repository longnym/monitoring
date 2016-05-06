package com.sk.collect.monitor.vo;

public class Schedule {
	private final String group;
	private final String job;
	private final String start;

	public Schedule(String group, String job, String start) {
		this.group = group;
		this.job = job;
		this.start = start;
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
}