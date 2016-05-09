package com.sk.collect.monitor.vo;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
	private long schdId;
	private String schdNm;
	private String cron;
	private List<Node> nodes;

	public Schedule() {
		nodes = new ArrayList<Node>();
	}

	public long getSchdId() {
		return schdId;
	}

	public void setSchdId(long schdId) {
		this.schdId = schdId;
	}

	public String getSchdNm() {
		return schdNm;
	}

	public void setSchdNm(String schdNm) {
		this.schdNm = schdNm;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron + " ?";
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
}