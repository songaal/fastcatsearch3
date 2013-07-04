/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.vo.JobHistoryVO;

public class JobHistory extends DAOBase {

	public JobHistory(ConnectionManager connectionManager) {
		super(connectionManager);
	}

	@Override
	public boolean testTable() {
		return testQuery("select id, jobId, jobClassName, args, isSuccess, resultStr, isScheduled, startTime, endTime, duration from " + tableName);
	}
	
	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table "
				+ tableName
				+ "(id int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) primary key, jobId bigint, jobClassName varchar(200), args varchar(3000), isSuccess smallint, resultStr varchar(3000), isScheduled smallint, startTime timestamp, endTime timestamp, duration int)";
		executeUpdate(createSQL);
		return true;
	}

	public int insert(long jobId, String jobClassName, String args, boolean isSuccess, String resultStr, boolean isScheduled,
			Timestamp startTime, Timestamp endTime, int duration) {

		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			conn = conn();

			String insertSQL = "insert into "
					+ tableName
					+ "(jobId, jobClassName, args, isSuccess, resultStr, isScheduled, startTime, endTime, duration) values (?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setLong(parameterIndex++, jobId);
			pstmt.setString(parameterIndex++, jobClassName);
			pstmt.setString(parameterIndex++, args);
			pstmt.setBoolean(parameterIndex++, isSuccess);
			pstmt.setString(parameterIndex++, resultStr);
			pstmt.setBoolean(parameterIndex++, isScheduled);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setTimestamp(parameterIndex++, endTime);
			pstmt.setInt(parameterIndex++, duration);
			int c = pstmt.executeUpdate();
			return c;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public List<JobHistoryVO> select(int startRow, int length) {
		String selectSQL = "SELECT id, jobId, jobClassName, args, isSuccess, resultStr, isScheduled, startTime, endTime, duration"
				+ " FROM ( SELECT ROW_NUMBER() OVER() AS rownum, "
				+ tableName
				+ ".* FROM "
				+ tableName
				+ " ) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<JobHistoryVO> result = new ArrayList<JobHistoryVO>();

		try {
			int totalCount = selectCount();
			
			conn = conn();

			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
//			logger.debug("Start = " + (totalCount - length) + "~" + (totalCount - startRow));
			while (rs.next()) {
				JobHistoryVO r = new JobHistoryVO();

				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.jobId = rs.getLong(parameterIndex++);
				r.jobClassName = rs.getString(parameterIndex++);
				r.args = rs.getString(parameterIndex++);
				r.isSuccess = rs.getBoolean(parameterIndex++);
				r.resultStr = rs.getString(parameterIndex++);
				r.isScheduled = rs.getBoolean(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.endTime = rs.getTimestamp(parameterIndex++);
				r.duration = rs.getInt(parameterIndex++);

				result.add(r);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}

		return result;
	}

}
