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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.vo.IndexingScheduleVO;

public class IndexingSchedule extends DAOBase {

	public IndexingSchedule(ConnectionManager connectionManager) {
		super(connectionManager);
	}

	@Override
	public boolean testTable() {
		return testQuery("select count(*) from " + tableName);
	}

	public boolean createTable() throws SQLException {
		String createSQL = "create table " + tableName
					+ "(collection varchar(20), type char(4), period int, startTime timestamp, isActive smallint"
					+ ",PRIMARY KEY (collection, type))";
		executeUpdate(createSQL);
		return true;
	}

	public synchronized int delete(String collection) {
		Connection conn = null;
		int result = 0;
		PreparedStatement pstmt = null;
		try {
			conn = conn();
			String deleteSQL = "delete from " + tableName + " where collection = ?";
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return result;
	}

	public synchronized int deleteByType(String collection, String type) {
		String deleteSQL = "delete from " + tableName + " where collection = ? and type = ? ";

		int result = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = conn();
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return result;
	}

	public synchronized int updateOrInsert(String collection, String type, int period, Timestamp startTime, boolean isActive) {
		String checkSQL = "select count(collection) from " + tableName + " " + "where collection=? and type=?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int result = 0;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(checkSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			rs = pstmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}

			if (count > 0) {
				result = update(collection, type, period, startTime, isActive);
			} else {
				result = insert(collection, type, period, startTime, isActive);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			result = -1;
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
		return result;
	}

	public synchronized int updateStatus(String collection, String type, boolean isActive) {
		int result = -1;
		String updateSQL = "update " + tableName + " set isActive=? " + "where collection=? and type=?";

		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setBoolean(parameterIndex++, isActive);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			result = -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return result;
	}

	public synchronized int insert(String collection, String type, int period, Timestamp startTime, boolean isActive) {
		int result = -1;
		String insertSQL = "insert into " + tableName + "(collection, type, period, startTime, isActive) values (?,?,?,?,?)";

		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			pstmt.setInt(parameterIndex++, period);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setBoolean(parameterIndex++, isActive);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			result = -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return result;
	}

	public synchronized int update(String collection, String type, int period, Timestamp startTime, boolean isActive) {
		int result = -1;
		String updateSQL = "update " + tableName + " set period=?, startTime=?, isActive=? " + "where collection=? and type=?";
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, period);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setBoolean(parameterIndex++, isActive);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			result = -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return result;
	}

	public IndexingScheduleVO select(String collection, String type) {
		String selectSQL = "select collection, type, period, startTime, isActive from " + tableName + " "
				+ "where collection=? and type=?";

		IndexingScheduleVO r = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(selectSQL);

			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				r = new IndexingScheduleVO();

				parameterIndex = 1;
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.period = rs.getInt(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.isActive = rs.getBoolean(parameterIndex++);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
		return r;
	}

	public List<IndexingScheduleVO> selectAll() {
		String selectSQL = "select collection, type, period, startTime, isActive from " + tableName + " where isActive = 1";

		List<IndexingScheduleVO> result = new ArrayList<IndexingScheduleVO>();
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(selectSQL);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				IndexingScheduleVO r = new IndexingScheduleVO();

				int parameterIndex = 1;
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.period = rs.getInt(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.isActive = rs.getBoolean(parameterIndex++);

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