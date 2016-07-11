package com.sk.collect.monitor.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.scheduling.quartz.QuartzJobBean;

import com.sk.collect.monitor.service.ElasticsearchService;

public class ElasticsearchCountJob extends QuartzJobBean {
	private ElasticsearchService elasticsearchService;

	public void setElasticsearchService(ElasticsearchService elasticsearchService) {
		this.elasticsearchService = elasticsearchService;
	}

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		String index = (String) context.getMergedJobDataMap().get("index");
		String[] type = (String[]) context.getMergedJobDataMap().get("type");
		elasticsearchService.esTotalCount(index, type);
	}
}