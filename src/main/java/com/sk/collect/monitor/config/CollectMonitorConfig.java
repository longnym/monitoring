package com.sk.collect.monitor.config;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.sk.collect.monitor.service.ElasticsearchService;

@Configuration
public class CollectMonitorConfig {
	@Autowired
	private ElasticsearchService elasticsearchService;

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();

		Map<String, ElasticsearchService> props = new HashMap<String, ElasticsearchService>();
		props.put("elasticsearchService", elasticsearchService);
		scheduler.setSchedulerContextAsMap(props);

		return scheduler;
	}
}