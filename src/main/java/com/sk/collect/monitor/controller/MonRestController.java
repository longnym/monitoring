package com.sk.collect.monitor.controller;

import java.util.List;

import com.sk.collect.monitor.vo.Count;
import com.sk.collect.monitor.service.ElasticsearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class MonRestController {
	@Autowired
	private ElasticsearchService elasticsearchService;
	
	// elasticsearch에 카운트를 저장
	@RequestMapping(value = "/mon/putCount", method = RequestMethod.POST)
	public void putCount(@RequestBody String message) {
		elasticsearchService.indexCount(message);
	}

	// elasticsearch의 카운트 데이터를 삭제
	@RequestMapping("/mon/delCount")
	public void delCount(
			@RequestParam(value = "date_from", defaultValue = "") String date_from,
			@RequestParam(value = "date_to", defaultValue = "") String date_to) {
		elasticsearchService.deleteCount(date_from, date_to);
	}

	// elasticsearch의 카운트를 조회
	@RequestMapping("/mon/getCount")
	public @ResponseBody List<Count> getCount(
			@RequestParam(value = "date_from", defaultValue = "") String date_from,
			@RequestParam(value = "date_to", defaultValue = "") String date_to,
			@RequestParam(value = "type", defaultValue = "") String type,
			@RequestParam(value = "host", defaultValue = "") String host,
			@RequestParam(value = "source", defaultValue = "") String source) {
		return elasticsearchService.searchCount(date_from, date_to, type, host);
	}
	
	// elasticsearch의 전체 건수를 조회
	@RequestMapping("/mon/esCount")
	public @ResponseBody Count esCount() {
		return elasticsearchService.esCount();
	}
}