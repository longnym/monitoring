package com.sk.collect.monitor.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sk.collect.monitor.vo.Node;
import com.sk.collect.monitor.vo.Schedule;
import com.sk.collect.monitor.vo.IndexInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class JdbcService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 등록된 스케줄에 설정되어 있는 Job들을 검색
	public Schedule searchSchedule(long schdId) {
		String sql = "SELECT SCHEDULE_ID, SCHEDULE_NM, SCHEDULE_CRON FROM SCHEDULE_MASTER WHERE SCHEDULE_ID = ?";
		Schedule schd = jdbcTemplate.query(sql, new Object[] { schdId }, new ResultSetExtractor<Schedule>() {
			@Override
			public Schedule extractData(ResultSet rs) throws SQLException {
				rs.next();
				Schedule tmpSchd = new Schedule();
				tmpSchd.setSchdId(rs.getLong("SCHEDULE_ID"));
				tmpSchd.setSchdNm(rs.getString("SCHEDULE_NM"));
				tmpSchd.setCron(rs.getString("SCHEDULE_CRON"));

				return tmpSchd;
			}
		});

		sql = "SELECT NODE_SEQ, NODE_NM, NODE_TYPE FROM NODE_MASTER WHERE SCHEDULE_MASTER_SCHEDULE_ID = ? ORDER BY NODE_SEQ ASC";
		List<Node> nodes = jdbcTemplate.query(sql, new Object[] { schdId }, new ResultSetExtractor<List<Node>>() {
			@Override
			public List<Node> extractData(ResultSet rs) throws SQLException {
				List<Node> tmpList = new ArrayList<Node>();
				while (rs.next()) {
					Node tmpNode = new Node();
					tmpNode.setNodeSeq(rs.getString("NODE_SEQ"));
					tmpNode.setNodeNm(rs.getString("NODE_NM"));
					tmpNode.setNodeType(rs.getString("NODE_TYPE"));
					tmpList.add(tmpNode);
				}
				return tmpList;
			}
		});

		sql = "SELECT CONF_CD, CONF_VALUE FROM NODE_DETAIL WHERE SCHEDULE_MASTER_SCHEDULE_ID = ? AND NODE_MASTER_NODE_SEQ = ?";
		for (Node node : nodes) {
			jdbcTemplate.query(sql, new Object[] { schdId, node.getNodeSeq() }, new ResultSetExtractor<String>() {
				@Override
				public String extractData(ResultSet rs) throws SQLException {
					while (rs.next()) {
						node.addProperty(rs.getString("CONF_CD"), rs.getString("CONF_VALUE"));
					}
					return "";
				}
			});
			schd.setNodes(nodes);
		}

		return schd;
	}

	// 등록된 스케줄 목록을 검색
	public List<Schedule> searchScheduleList() {
		String sql = "SELECT SCHEDULE_ID FROM SCHEDULE_MASTER WHERE USE_YN = 'Y'";
		List<Long> idList = jdbcTemplate.query(sql, new ResultSetExtractor<List<Long>>() {
			@Override
			public List<Long> extractData(ResultSet rs) throws SQLException {
				List<Long> tmpList = new ArrayList<Long>();
				while (rs.next()) {
					tmpList.add(rs.getLong("SCHEDULE_ID"));
				}
				return tmpList;
			}
		});

		List<Schedule> schdList = new ArrayList<Schedule>();
		for (long schdId : idList) {
			schdList.add(searchSchedule(schdId));
		}

		return schdList;
	}

	// 호스트 목록을 가져옴
	public String searchHostList(int service, String index) {
		String sql = "SELECT DISTINCT(HOST) FROM COLLECT_AGENT_MASTER M JOIN COLLECT_AGENT_DETAIL D ON M.AGENT_ID = D.AGENT_ID WHERE SVC_ID LIKE ? AND INDEX_NM LIKE ? ORDER BY HOST ASC";
		return jdbcTemplate.query(sql, new Object[] { service, index }, new ResultSetExtractor<String>() {
			@Override
			public String extractData(ResultSet rs) throws SQLException {
				String tmp = "";
				if (rs.next()) {
					tmp = rs.getString("HOST");
				}
				while (rs.next()) {
					tmp += "," + rs.getString("HOST");
				}
				return tmp;
			}
		});
	}

	// 인덱스 목록을 가져옴
	public List<IndexInfo> searchIndexList(int service, String host) {
		String sql = "SELECT INDEX_NM, AGENT_TYPE, HOST FROM COLLECT_AGENT_MASTER M JOIN COLLECT_AGENT_DETAIL D ON M.AGENT_ID = D.AGENT_ID WHERE SVC_ID LIKE ? AND HOST LIKE ?";
		return jdbcTemplate.query(sql, new Object[] { service, host }, new ResultSetExtractor<List<IndexInfo>>() {
			@Override
			public List<IndexInfo> extractData(ResultSet rs) throws SQLException {
				List<IndexInfo> tmpList = new ArrayList<IndexInfo>();
				while (rs.next()) {
					IndexInfo tmpInfo = new IndexInfo();
					tmpInfo.setIndexNm(rs.getString("INDEX_NM"));
					tmpInfo.setAgentType(rs.getString("AGENT_TYPE"));
					tmpInfo.setHost(rs.getString("HOST"));
					tmpList.add(tmpInfo);
				}
				return tmpList;
			}
		});
	}

	// 스케줄 Node의 실행 결과를 저장
	public void insertNodeHistory(long schdId, String nodeSeq, long startDt, long endDt, int resCode) {
		String success = "Y";
		String message = "";
		if(resCode != 0) {
			success = "N";
			message = "ERROR";
		}
		
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql = "INSERT INTO SCHEDULE_NODE_RUN_HISTORY VALUES(?, ?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(sql, schdId, nodeSeq, sf1.format(new Date(startDt)), sf1.format(new Date(endDt)), success, message, sf2.format(new Date(System.currentTimeMillis())));
	}
}