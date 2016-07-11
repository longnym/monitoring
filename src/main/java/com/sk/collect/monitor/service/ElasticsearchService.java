package com.sk.collect.monitor.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.sk.collect.monitor.vo.Count;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;

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

	@Value("${elasticsearch.type}")
	private String esType;

	private long esTotal;
	
	private final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public void init() {
		esTotal = 0;
	}

	// 커운트 정보를 저장
	public void indexCount(String message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Count cnt = mapper.readValue(message, Count.class);
			message = mapper.writeValueAsString(cnt);

			IndexQuery query = new IndexQuery();
			query.setIndexName(esIndex);
			query.setType(esType);
			query.setSource(message);

			elasticsearchTemplate.index(query);

			System.out.println("Indexed message: " + message);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 커운트 정보를 삭제
	public void deleteCount(String date_from, String date_to) {
		DeleteQuery query = new DeleteQuery();
		query.setIndex(esIndex);
		query.setType(esType);
		query.setQuery(QueryBuilders.rangeQuery("dt").from(date_from).to(date_to));

		elasticsearchTemplate.delete(query);
	}

	// Interval 옵션을 millisecond 시간 단위로 변환 
	public long convertTime(String interval) {
		long intervalSec = 1000;

		String timeTerm = interval.substring(interval.length() - 1).toLowerCase();
		int timeNumber = Integer.parseInt(interval.substring(0, interval.length() - 1));

		if(timeTerm.equals("s")) {
			intervalSec = intervalSec * timeNumber;
		} else if (timeTerm.equals("m")) {
			intervalSec = intervalSec * timeNumber * 60;
		} else if (timeTerm.equals("h")) {
			intervalSec = intervalSec * timeNumber * 60 * 60;
		} else if (timeTerm.equals("d")) {
			intervalSec = intervalSec * timeNumber * 60 * 60 * 24;
		}

		return intervalSec;
	}

	// Interval 주기 맞는 date bound로 변환 
	public String convertDate(String date, long interval) {
		SimpleDateFormat sf = new SimpleDateFormat(TIME_FORMAT);
		try {
			long dateMill = sf.parse(date).getTime() / interval * interval;
			Date newDate = new Date(dateMill);

			return sf.format(newDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 커운트 정보를 시간단위로 집계
	public List<Count> searchCount(String date_from, String date_to, int service, String host, String type, String source, String interval) {
		long intervalSec = convertTime(interval);
		String date_from_cvt = convertDate(date_from, intervalSec);
		String date_to_cvt = convertDate(date_to, intervalSec);

		SumBuilder sumCount = AggregationBuilders.sum("aggCount").field("cnt");

		// Date Histogram으로 Aggregation 후 시간별 합계를 구함
		DateHistogramBuilder aggDate = AggregationBuilders
				.dateHistogram("aggDate")
				.field("dt")
				.subAggregation(sumCount)
				.interval(intervalSec)
				.extendedBounds(date_from_cvt, date_to_cvt)
				.minDocCount(0);

		SearchQuery query = new NativeSearchQueryBuilder()
				.withIndices(esIndex)
				.withTypes(esType)
				.withQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.wildcardQuery("host", host))
						.must(QueryBuilders.wildcardQuery("typ", type))
						.must(QueryBuilders.wildcardQuery("src", source))
						.must(QueryBuilders.rangeQuery("dt")
								.from(date_from_cvt)
								.to(date_to)))
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

			Count cnt = new Count();
			cnt.setDt(key);
			cnt.setSvc(service);
			cnt.setHost(host);
			cnt.setTyp(type);
			cnt.setSrc(source);
			cnt.setCyc((int) intervalSec / 1000);
			cnt.setCnt((long) sum.value());

			cntList.add(cnt);
		}
		return cntList;
	}

	// 커운트 정보의 필드를 Grouping 하여 시간단위로 집계
	public List<Count> searchGroupCount(String date_from, String date_to, int service, String host, String type, String source, String interval, String group) {
		long intervalSec = convertTime(interval);
		String date_from_cvt = convertDate(date_from, intervalSec);
		String date_to_cvt = convertDate(date_to, intervalSec);

		SumBuilder sumCount = AggregationBuilders.sum("aggCount").field("cnt");

		// Date Histogram으로 Aggregation 후 시간별 합계를 구함
		DateHistogramBuilder aggDate = AggregationBuilders
				.dateHistogram("aggDate")
				.field("dt")
				.subAggregation(sumCount)
				.interval(intervalSec)
				.extendedBounds(date_from_cvt, date_to_cvt)
				.minDocCount(0);

		// 지정한 필드에 대해 Grouping을 함
		TermsBuilder aggField = AggregationBuilders
				.terms("aggField")
				.field(group)
				.subAggregation(aggDate);

		SearchQuery query = new NativeSearchQueryBuilder()
				.withIndices(esIndex)
				.withTypes(esType)
				.withQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.wildcardQuery("host", host))
						.must(QueryBuilders.wildcardQuery("typ", type))
						.must(QueryBuilders.wildcardQuery("src", source))
						.must(QueryBuilders.rangeQuery("dt")
								.from(date_from_cvt)
								.to(date_to)))
				.addAggregation(aggField)
				.build();

		Terms result = elasticsearchTemplate.query(query, new ResultsExtractor<Terms>() {
			@Override
			public Terms extract(SearchResponse response) {
				return response.getAggregations().get("aggField");
			}
		});

		List<Count> cntList = new ArrayList<Count>();
		for (Terms.Bucket entryTerm : result.getBuckets()) {
			String keyTerm = entryTerm.getKeyAsString();
			if(keyTerm.length() == 0) {
				continue;
			}

			Histogram histogram = entryTerm.getAggregations().get("aggDate");
			for (Histogram.Bucket entryDate : histogram.getBuckets()) {
				String keyDate = entryDate.getKeyAsString();
				Sum sum = entryDate.getAggregations().get("aggCount");

				Count cnt = new Count();
				cnt.setDt(keyDate);
				cnt.setSvc(service);
				cnt.setHost(host);
				cnt.setTyp(type);
				cnt.setSrc(source);

				if(group.equals("host")) {
					cnt.setHost(keyTerm);
				} else if (group.equals("typ")) {
					cnt.setTyp(keyTerm);
				} else if (group.equals("src")) {
					cnt.setSrc(keyTerm);
				}

				cnt.setCyc((int) intervalSec / 1000);
				cnt.setCnt((long) sum.value());

				cntList.add(cnt);
			}
		}
		return cntList;
	}
	
	// HeartBeat 체크
	public List<Count> checkHeartbeat(int range, int service, String interval) {
		SimpleDateFormat sf = new SimpleDateFormat(TIME_FORMAT);
		long intervalSec = convertTime(interval);

		long current = System.currentTimeMillis();
		long timeTo = current / intervalSec * intervalSec;
		long timeFrom = timeTo - (range * 1000);
		String date_from = sf.format(new Date(timeFrom));
		String date_to = sf.format(new Date(current));

		SumBuilder sumHb = AggregationBuilders.sum("aggHb").field("hb");

		TermsBuilder aggHost = AggregationBuilders
				.terms("aggHost")
				.field("host")
				.subAggregation(sumHb);

		TermsBuilder aggType = AggregationBuilders
				.terms("aggType")
				.field("typ")
				.subAggregation(aggHost);

		TermsBuilder aggAgent = AggregationBuilders
				.terms("aggAgent")
				.field("agnt")
				.subAggregation(aggType);

		SearchQuery query = new NativeSearchQueryBuilder()
				.withIndices(esIndex)
				.withTypes(esType)
				.withQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.rangeQuery("dt")
								.from(date_from)
								.to(date_to)))
				.addAggregation(aggAgent)
				.build();

		Terms result = elasticsearchTemplate.query(query, new ResultsExtractor<Terms>() {
			@Override
			public Terms extract(SearchResponse response) {
				return response.getAggregations().get("aggAgent");
			}
		});

		List<Count> cntList = new ArrayList<Count>();
		for (Terms.Bucket entryAgent : result.getBuckets()) {
			String keyAgent = entryAgent.getKeyAsString();
			Terms termType = entryAgent.getAggregations().get("aggType");
			for (Terms.Bucket entryType : termType.getBuckets()) {
				String keyType = entryType.getKeyAsString();
				Terms termHost = entryType.getAggregations().get("aggHost");
				for (Terms.Bucket entryHost : termHost.getBuckets()) {
					String keyHost = entryHost.getKeyAsString();
					Sum sum = entryHost.getAggregations().get("aggHb");

					Count cnt = new Count();
					cnt.setSvc(service);
					cnt.setHost(keyHost);
					cnt.setTyp(keyType);
					cnt.setAgnt(keyAgent);
					cnt.setHb((int)sum.value());

					cntList.add(cnt);
				}
			}
		}
		return cntList;
	}

	// Elasticsearch의 전체 건수 변화량을 측정하여 저장
	public void esTotalCount(String index, String[] type) {
		SimpleDateFormat sf = new SimpleDateFormat(TIME_FORMAT);

		SearchQuery query = new NativeSearchQueryBuilder()
				.withIndices(index)
				//.withTypes(type)
				.build();

		long curTotal = elasticsearchTemplate.count(query);
		if (esTotal == 0L) {
			System.out.println("Elasticsearch counter is initialized.");
			esTotal = curTotal;
			return;
		}

		Count cnt = new Count();
		cnt.setDt(sf.format(new Date()));
		cnt.setSvc(0);
		cnt.setTyp("es");
		cnt.setCyc(1);
		cnt.setCnt(curTotal - esTotal);
		cnt.setHb(1);

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

	// SQL의 조회 결과를 저장
	public String indexJobResult(String message, String saveIndex, String saveType, String ttl) {
		// Elasticsearch에서 '.'이 포함된 필드명을 지원하지 않으므로 '_'로 변환
		String regex = "[\"]{1}[A-Za-z0-9 ]*[.]{1}[A-Za-z0-9 ]*[\"]{1}[:]{1}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(message);

		while(matcher.find()) {
			String matchStr = matcher.group();
			String replaceStr = matchStr.replace(".", "_");
			message = message.replaceAll(matchStr, replaceStr);
		}

		IndexQuery query = new IndexQuery();
		query.setIndexName(saveIndex);
		query.setType(saveType);
		query.setSource(message);

		if(!elasticsearchTemplate.indexExists(saveIndex)) {
			elasticsearchTemplate.createIndex(saveIndex);
		}

		if(ttl.equals("Y")) {
			String ttlMapping = "{\"_ttl\":{\"enabled\":true,\"default\":\"1m\"}}";
			elasticsearchTemplate.putMapping(saveIndex, saveType, ttlMapping);
		}

		String docId = null;
		try {
			docId = elasticsearchTemplate.index(query);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("Document ID: " + docId);
		System.out.println("Indexed message: " + message);
		
		return docId;
	}
}