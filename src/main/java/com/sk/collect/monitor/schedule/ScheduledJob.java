package com.sk.collect.monitor.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.sk.collect.monitor.service.ElasticsearchService;
import com.sk.collect.monitor.vo.Node;
import com.sk.collect.monitor.vo.Schedule;

public class ScheduledJob extends QuartzJobBean {
	private ElasticsearchService elasticsearchService;

	public void setElasticsearchService(ElasticsearchService elasticsearchService) {
		this.elasticsearchService = elasticsearchService;
	}

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		Schedule schd = (Schedule) context.getMergedJobDataMap().get("schdMeta");

		for (Node node : schd.getNodes()) {
			String type = node.getNodeType();
			if (type.equals("ES Query")) {
				String searchHost = (String) context.getMergedJobDataMap().get("searchHost");
				String searchQuery = node.getProperty("query");
				String saveIndex = node.getProperty("index");
				String saveType = node.getProperty("type");

				elasticsearchJob(searchHost, searchQuery, saveIndex, saveType);
			} else if (type.equals("JAR")) {
			} else if (type.equals("SMS")) {
			} else {

			}
		}
	}

	public void elasticsearchJob(String host, String query, String index, String type) {
		try {
			String result = requestData(host, query);
			elasticsearchService.indexJobResult(result, index, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String requestData(String searchHost, String query) throws IOException {
		URL obj = new URL("http://" + searchHost + "/_sql");
		System.out.println("Sending 'GET' request to URL : " + searchHost);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		OutputStream os = conn.getOutputStream();
		os.write(query.getBytes());
		os.flush();

		int responseCode = conn.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}
}