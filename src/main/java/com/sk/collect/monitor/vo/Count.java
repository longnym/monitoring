package com.sk.collect.monitor.vo;

public class Count {
	private final String date;
	private final String type;
	private final String host;
	private final long count;

	public Count(String date, String type, String host, long count) {
		this.date = date;
		this.type = type;
		this.host = host;
		this.count = count;
	}

	public String getDate() {
		return date;
	}

	public String getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public long getCount() {
		return count;
	}
}