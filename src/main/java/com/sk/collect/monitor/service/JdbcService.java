package com.sk.collect.monitor.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sk.collect.monitor.vo.Node;
import com.sk.collect.monitor.vo.Schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class JdbcService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

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

		sql = "SELECT NODE_ID, NODE_NM, NODE_TYPE FROM NODE_MASTER WHERE SCHEDULE_MASTER_SCHEDULE_ID = ? ORDER BY RUN_SEQ ASC";
		List<Node> nodes = jdbcTemplate.query(sql, new Object[] { schdId }, new ResultSetExtractor<List<Node>>() {
			@Override
			public List<Node> extractData(ResultSet rs) throws SQLException {
				List<Node> tmpList = new ArrayList<Node>();
				while (rs.next()) {
					Node tmpNode = new Node();
					tmpNode.setNodeId(rs.getString("NODE_ID"));
					tmpNode.setNodeNm(rs.getString("NODE_NM"));
					tmpNode.setNodeType(rs.getString("NODE_TYPE"));
					tmpList.add(tmpNode);
				}
				return tmpList;
			}
		});

		sql = "SELECT CONF_CD, CONF_VALUE FROM NODE_DETAIL WHERE NODE_MASTER_NODE_ID = ?";
		for (Node node : nodes) {
			jdbcTemplate.query(sql, new Object[] { schdId }, new ResultSetExtractor<String>() {
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
}