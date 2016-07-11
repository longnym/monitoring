package com.sk.collect.monitor.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.sk.collect.monitor.vo.Count;
import com.sk.collect.monitor.vo.IndexInfo;
import com.sk.collect.monitor.service.ElasticsearchService;
import com.sk.collect.monitor.service.JdbcService;
import com.sk.collect.monitor.service.SchedulerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mon")
public class MonRestController {
	@Value("${schedule.monitor.elasticsearch}")
	private String esMon;

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private SchedulerService schedulerService;

	@Autowired
	private JdbcService jdbcService;

	@PostConstruct
	public void init() {
		if(esMon.equals("true")) {
			startEsCount(0);
		}
	}

	// elasticsearch에 카운트를 저장
	@RequestMapping(value = "/putCount", method = RequestMethod.POST)
	public void putCount(@RequestBody String message) {
		elasticsearchService.indexCount(message);
	}

	// elasticsearch의 카운트 데이터를 삭제
	@RequestMapping("/delCount")
	public void delCount(
			@RequestParam(value = "date_from", defaultValue = "") String date_from,
			@RequestParam(value = "date_to", defaultValue = "") String date_to) {
		if (date_from.equals("") || date_to.equals("")) {
			return;
		}
		elasticsearchService.deleteCount(date_from, date_to);
	}

	// elasticsearch의 카운트를 조회
	@RequestMapping("/getCount")
	public @ResponseBody List<Count> getCount(
			@RequestParam(value = "date_from", defaultValue = "") String date_from,
			@RequestParam(value = "date_to", defaultValue = "") String date_to,
			@RequestParam(value = "service", defaultValue = "0") int service,
			@RequestParam(value = "host", defaultValue = "*") String host,
			@RequestParam(value = "type", defaultValue = "*") String type,
			@RequestParam(value = "source", defaultValue = "*") String source,
			@RequestParam(value = "interval", defaultValue = "5s") String interval,
			@RequestParam(value = "group", defaultValue = "") String group) {
		if (date_from.equals("") || date_to.equals("")) {
			return null;
		}
		if (group.equals("")) {
			return elasticsearchService.searchCount(date_from, date_to, service, host, type, source, interval);
		} else {
			group = group.replace("type", "typ");
			group = group.replace("source", "src");
			return elasticsearchService.searchGroupCount(date_from, date_to, service, host, type, source, interval, group);
		}
	}

	// Heartbeat를 집계
	@RequestMapping("/checkStatus")
	public @ResponseBody List<Count> checkStatus(
			@RequestParam(value = "range", defaultValue = "20") int range,
			@RequestParam(value = "service", defaultValue = "0") int service,
			@RequestParam(value = "interval", defaultValue = "5s") String interval) {
		return elasticsearchService.checkHeartbeat(range, service, interval);
	}

	// elasticsearch의 저장 건수를 계산하여 저장하는 스케줄러 구동
	@RequestMapping("/esCount/start")
	public void startEsCount(@RequestParam(value = "service", defaultValue = "0") int service) {
		elasticsearchService.init();

		List<IndexInfo> indexInfos = jdbcService.searchIndexList(service, "%");
		Set<String> tmpSet = new HashSet<String>();		// 중복 제거
		for(IndexInfo indexInfo : indexInfos) {
			tmpSet.add(indexInfo.getIndexNm());
		}

		schedulerService.scheduleEsCount("mas", tmpSet.toArray(new String[tmpSet.size()]));
		System.out.println("elasticsearch counter is started.");
	}

	// elasticsearch의 저장 건수를 계산하여 저장하는 스케줄러 중지
	@RequestMapping("/esCount/stop")
	public void stopEsCount() {
		schedulerService.unscheduleEsCount();
		System.out.println("elasticsearch counter is stopped.");
	}

	// 수집 호스트의 목록을 불러옴
	@CrossOrigin(origins = "http://localhost:9898")
	@RequestMapping("/getHost")
	public @ResponseBody String getHost(
			@RequestParam(value = "service", defaultValue = "0") int service,
			@RequestParam(value = "index", defaultValue = "%") String index) {
		return jdbcService.searchHostList(service, index);
	}

	// 저장 인덱스의 목록을 불러옴
	@RequestMapping("/getIndex")
	public @ResponseBody List<IndexInfo> getIndex(
			@RequestParam(value = "service", defaultValue = "0") int service,
			@RequestParam(value = "host", defaultValue = "%") String host) {
		return jdbcService.searchIndexList(service, host);
	}
}