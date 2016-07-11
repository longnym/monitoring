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

import com.sk.collect.monitor.service.LauncherService;
import com.sk.collect.monitor.service.ElasticsearchService;
import com.sk.collect.monitor.service.JdbcService;
import com.sk.collect.monitor.vo.Node;
import com.sk.collect.monitor.vo.Schedule;

public class ScheduledJob extends QuartzJobBean {
	private ElasticsearchService elasticsearchService;
	private LauncherService launcherService;
	private JdbcService jdbcService;

	public void setElasticsearchService(ElasticsearchService elasticsearchService) {
		this.elasticsearchService = elasticsearchService;
	}

	public void setLauncherService(LauncherService launcherService) {
		this.launcherService = launcherService;
	}

	public void setJdbcService(JdbcService jdbcService) {
		this.jdbcService = jdbcService;
	}

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		Schedule schd = (Schedule) context.getMergedJobDataMap().get("schdMeta");

		int resCode = 0;
		for (Node node : schd.getNodes()) {
			String type = node.getNodeType();
			long startDt = System.currentTimeMillis();

			if (type.equals("ES Query") && resCode == 0) {
				String searchHost = (String) context.getMergedJobDataMap().get("searchHost");
				String searchQuery = node.getProperty("query");
				String saveIndex = node.getProperty("index");
				String saveType = node.getProperty("type");
				String ttlExist = node.getProperty("ttl");

				resCode = elasticsearchJob(searchHost, searchQuery, saveIndex, saveType, ttlExist);
			} else if (type.equals("Jar File") && resCode == 0) {
				String path = node.getProperty("path");
				String option = node.getProperty("option");

				resCode = jarLauncherJob(path, option);
			} else if (type.equals("Shell Script") && resCode == 0) {
				String cmd = node.getProperty("command");

				resCode = cmdLauncherJob(cmd);
			} else {
				System.out.println("Cannot execute job: " + schd.getSchdId() + "_" + node.getNodeSeq());
				resCode = -1;
			}

			long endDt = System.currentTimeMillis();
			jdbcService.insertNodeHistory(schd.getSchdId(), node.getNodeSeq(), startDt, endDt, resCode);
			if (resCode != 0) {
				break;
			}
		}
	}

	public int elasticsearchJob(String host, String query, String index, String type, String ttl) {
		try {
			String result = requestData(host, query);

			if (result == null) {
				return -1;
			}
			String docId = elasticsearchService.indexJobResult(result, index, type, ttl);
			if (docId == null) {
				return -1;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public int jarLauncherJob(String path, String option) {
		String result = launcherService.runJar(path, option);
		System.out.println("Jar Result: " + result);
		return 0;
	}

	public int cmdLauncherJob(String cmd) {
		String result = launcherService.executeCommand(cmd);
		System.out.println("Command Result: " + result);
		return 0;
	}

	public String requestData(String searchHost, String query) throws IOException {
		URL obj = new URL("http://" + searchHost + "/_sql");
		System.out.println("Sending 'GET' request to URL: " + searchHost);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		OutputStream os = conn.getOutputStream();
		os.write(query.getBytes());
		os.flush();

		int responseCode = conn.getResponseCode();
		System.out.println("Response Code: " + responseCode);

		if (responseCode != 200) {
			return null;
		}

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