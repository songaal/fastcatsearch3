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

public class IndexingResult extends DAOBase {

	public static int STATUS_FAIL = -1;
	public static int STATUS_SUCCESS = 0;
	public static int STATUS_RUNNING = 1;

	public String collection;
	public String type;
	public int status;
	public int docSize;
	public int updateSize;
	public int deleteSize;
	public boolean isScheduled;
	public Timestamp startTime;
	public Timestamp endTime;
	public int duration;

	public IndexingResult() {
	}

	@Override
	public boolean testTable() {
		return testQuery("select collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
				+ tableName + " where collection = '0'");
	}

	public boolean createTable() throws SQLException {
		String createSQL = "create table "
				+ tableName
				+ "(collection varchar(20), type char(1), status smallint, docSize int, updateSize int, deleteSize int, isScheduled smallint, startTime timestamp, endTime timestamp, duration int" +
				",CONSTRAINT primary_key PRIMARY KEY (collection, type))";

		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			stmt = conn.createStatement();
			stmt.executeUpdate(createSQL);
			return true;
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}

	public int repairStatus() throws SQLException {
		String repairSQL = "update " + tableName + " set status = " + IndexingResult.STATUS_FAIL + "  where status = "
				+ IndexingResult.STATUS_RUNNING;

		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			if (isExists() == true) {

				stmt = conn.createStatement();
				return stmt.executeUpdate(repairSQL);
			} else {
				return 0;
			}
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}

	public int updateOrInsert(String collection, String type, int status, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {

		String checkSQL = "select count(collection) from " + tableName + " where collection=? and type=?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

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
				return update(collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime,
						duration);
			} else {
				return insert(collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime,
						duration);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
	}

	public int insert(String collection, String type, int status, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {
		String insertSQL = "insert into "
				+ tableName
				+ "(collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration) values (?,?,?,?,?,?,?,?,?,?)";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			pstmt.setShort(parameterIndex++, (short) status);
			pstmt.setInt(parameterIndex++, docSize);
			pstmt.setInt(parameterIndex++, updateSize);
			pstmt.setInt(parameterIndex++, deleteSize);
			pstmt.setBoolean(parameterIndex++, isScheduled);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setTimestamp(parameterIndex++, endTime);
			pstmt.setInt(parameterIndex++, duration);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public int update(String collection, String type, int status, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {
		String updateSQL = "update " + tableName
				+ " set status=?, docSize=?, updateSize=?, deleteSize=?, isScheduled=?, startTime=?, endTime=?, duration=? "
				+ "where collection=? and type=?";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setShort(parameterIndex++, (short) status);
			pstmt.setInt(parameterIndex++, docSize);
			pstmt.setInt(parameterIndex++, updateSize);
			pstmt.setInt(parameterIndex++, deleteSize);
			pstmt.setBoolean(parameterIndex++, isScheduled);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setTimestamp(parameterIndex++, endTime);
			pstmt.setInt(parameterIndex++, duration);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public IndexingResult select(String collection, String type) {
		String selectSQL = "select collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
				+ tableName + " " + "where collection=? and type=?";

		IndexingResult r = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				r = new IndexingResult();

				parameterIndex = 1;
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.status = rs.getShort(parameterIndex++);
				r.docSize = rs.getInt(parameterIndex++);
				r.updateSize = rs.getInt(parameterIndex++);
				r.deleteSize = rs.getInt(parameterIndex++);
				r.isScheduled = rs.getBoolean(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.endTime = rs.getTimestamp(parameterIndex++);
				r.duration = rs.getInt(parameterIndex++);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}

		return r;
	}

	public int delete(String collection, String type) {
		String deleteSQL = "delete from " + tableName + " where collection=? and type=?";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			return pstmt.executeUpdate();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}

		return 0;
	}

	// 최근업데이트건의 최근 시간만 가져온다.
	public Timestamp isUpdated(Timestamp lastTime) {
		String checkSQL = "select endTime from " + tableName + " " + "where endTime > ? " + "order by endTime desc";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(checkSQL);
			pstmt.setTimestamp(1, lastTime);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getTimestamp(1);
			}

			return null;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
	}

}
