package com.sk.collect.monitor.config;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpServerConfig {
	@Value("${boss.thread.count}")
	private int bossCount;

	@Value("${worker.thread.count}")
	private int workerCount;

	@Value("${tcp.port}")
	private int tcpPort;

	public int getBossCount() {
		return bossCount;
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	@Bean
	public InetSocketAddress tcpSocketAddress() {
		return new InetSocketAddress(tcpPort);
	}
}
