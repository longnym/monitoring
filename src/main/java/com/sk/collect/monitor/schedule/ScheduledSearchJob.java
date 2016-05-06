package com.sk.collect.monitor.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.sk.collect.monitor.service.ElasticsearchService;
import com.sk.collect.monitor.vo.Job;

public class ScheduledSearchJob extends QuartzJobBean {
	private ElasticsearchService elasticsearchService;

	public void setElasticsearchService(ElasticsearchService elasticsearchService) {
		this.elasticsearchService = elasticsearchService;
	}

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		Job job = (Job) context.getMergedJobDataMap().get("jobMeta");
		String searchHost = (String) context.getMergedJobDataMap().get("searchHost");
		String saveIndex = (String) context.getMergedJobDataMap().get("saveIndex");
		String saveType = (String) context.getMergedJobDataMap().get("saveType");
		System.out.println(job.getQuery());
		try {
			String result = requestData("http://" + searchHost + "/_sql?sql=" + job.getQuery());
			elasticsearchService.indexJobResult(result, saveIndex, saveType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String requestData(String searchUrl) throws IOException {
		URL obj = new URL(searchUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + searchUrl);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}
}