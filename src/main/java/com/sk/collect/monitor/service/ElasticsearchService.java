package com.sk.collect.monitor.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.sk.collect.monitor.vo.Count;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

@Service
public class ElasticsearchService {
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Value("${elasticsearch.index}")
	private String esIndex;

	@Value("${elasticsearch.monitoring.indices}")
	private String[] monIndices;

	@Value("${elasticsearch.type}")
	private String esType;

	private long esTotal;

	public void indexCount(String message) {
		System.out.println("Get message from /mon/putCounter: " + message);

		IndexQuery query = new IndexQuery();
		query.setIndexName(esIndex);
		query.setType(esType);
		query.setSource(message);

		elasticsearchTemplate.index(query);
	}

	public void deleteCount(String date_from, String date_to) {
		DeleteQuery query = new DeleteQuery();
		query.setIndex(esIndex);
		query.setType(esType);
		query.setQuery(QueryBuilders.rangeQuery("@timestamp").from(date_from).to(date_to));

		elasticsearchTemplate.delete(query);
	}

	public List<Count> searchCount(String date_from, String date_to, String type, String host) {
		if (type.length() == 0) {	// type 필터 조건이 없을 경우 전체 검색
			type = "*";
		}

		if (host.length() == 0) {	// host 필터 조건이 없을 경우 전체 검색
			host = "*";
		}

		SumBuilder sumCount = AggregationBuilders.sum("aggCount").field("count");

		DateHistogramBuilder aggDate = AggregationBuilders	// Date Histogram으로 Aggregation 후 시간별 합계를 구함 
				.dateHistogram("aggDate")
				.field("@timestamp")
				.subAggregation(sumCount)
				.interval(DateHistogramInterval.SECOND);

		SearchQuery query = new NativeSearchQueryBuilder()
				.withIndices(esIndex)
				.withTypes(esType)
				.withQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.wildcardQuery("type", type))
						.must(QueryBuilders.wildcardQuery("host", host))
						.must(QueryBuilders.rangeQuery("@timestamp").from(date_from).to(date_to)))
				.addAggregation(aggDate)
				.build();

		Histogram result = elasticsearchTemplate.query(query, new ResultsExtractor<Histogram>() {
			@Override
			public Histogram extract(SearchResponse response) {
				return response.getAggregations().get("aggDate");
			}
		});

		List<Count> cntList = new ArrayList<Count>();
		for (Histogram.Bucket entry : result.getBuckets()) {
			String key = entry.getKeyAsString();
			Sum sum = entry.getAggregations().get("aggCount");
			cntList.add(new Count(key, type, host, (long)sum.value()));
		}

		return cntList;
	}

	public void esTotalCount() {
		SearchQuery query = new NativeSearchQueryBuilder()
				.withIndices(monIndices).build();

		long curTotal = elasticsearchTemplate.count(query);
		if(esTotal == 0L) {
			System.out.println("Elasticsearch counter is initialized.");
			esTotal = curTotal;
			return;
		}

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Count cnt = new Count(sf.format(new Date()), "elasticsearch", "localhost", curTotal - esTotal);

		ObjectMapper mapper = new ObjectMapper();
		String source = null;
		try {
			source = mapper.writeValueAsString(cnt);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		esTotal = curTotal;
		indexCount(source);
	}
}