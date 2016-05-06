package com.sk.collect.monitor.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sk.collect.monitor.vo.Job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class JdbcService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${schedule.meta.table}")
	private String metaTbl;

	public Job searchJob(long jobId) {
		String sql = "SELECT * FROM " + metaTbl + " WHERE jobid = " + jobId;
		return jdbcTemplate.query(sql, new ResultSetExtractor<Job>() {
			@Override
			public Job extractData(ResultSet rs) throws SQLException {
				rs.next();
				Job job = new Job();
				job.setJobId(rs.getLong("jobid"));
				job.setStatus(rs.getString("status"));
				job.setSchedule(rs.getString("schedule"));
				job.setQuery(rs.getString("query"));
				return job;
			}
		});
	}

	public List<Job> searchJobList() {
		String sql = "SELECT * FROM " + metaTbl;
		return jdbcTemplate.query(sql, new ResultSetExtractor<List<Job>>() {
			@Override
			public List<Job> extractData(ResultSet rs) throws SQLException {
				List<Job> jobList = new ArrayList<Job>();
				while (rs.next()) {
					Job job = new Job();
					job.setJobId(rs.getLong("jobid"));
					job.setStatus(rs.getString("status"));
					job.setSchedule(rs.getString("schedule"));
					job.setQuery(rs.getString("query"));
					jobList.add(job);
				}
				return jobList;
			}
		});
	}

	public void updateStartJob(long jobId) {
		String sql = "UPDATE " + metaTbl + " SET status = 'RUNNING' WHERE jobid = " + jobId;
		jdbcTemplate.update(sql);
	}

	public void updateStopJob(long jobId) {
		String sql = "UPDATE " + metaTbl + " SET status = 'STOP' WHERE jobid = " + jobId;
		jdbcTemplate.update(sql);
	}
}