package com.sk.collect.monitor.config;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.sk.collect.monitor.service.ElasticsearchService;
import com.sk.collect.monitor.service.LauncherService;
import com.sk.collect.monitor.service.JdbcService;

@Configuration
public class CollectMonitorConfig {
	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private LauncherService launcherService;

	@Autowired
	private JdbcService jdbcService;

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("elasticsearchService", elasticsearchService);
		props.put("launcherService", launcherService);
		props.put("jdbcService", jdbcService);
		scheduler.setSchedulerContextAsMap(props);

		return scheduler;
	}
}