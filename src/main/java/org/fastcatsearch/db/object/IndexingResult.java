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

package org.fastcatsearch.db.object;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.TreeSet;

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

	public int create() throws SQLException {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String createSQL = "create table "
					+ tableName
					+ "(collection varchar(20), type char(1), status smallint, docSize int, updateSize int, deleteSize int, isScheduled smallint, startTime timestamp, endTime timestamp, duration int)";
			stmt = conn.createStatement();
			return stmt.executeUpdate(createSQL);
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}

	public int repairStatus() throws SQLException {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			if (isExists() == true) {
				String repairSQL = "update " + tableName + " set status = " + IndexingResult.STATUS_FAIL + "  where status = "
						+ IndexingResult.STATUS_RUNNING;
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
		try {
			String checkSQL = "select count(collection) from " + tableName + " " + "where collection=? and type=?";
			PreparedStatement pstmt = conn.prepareStatement(checkSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			ResultSet rs = pstmt.executeQuery();
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
		}
	}

	public int insert(String collection, String type, int status, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {
		try {
			String insertSQL = "insert into "
					+ tableName
					+ "(collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration) values (?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(insertSQL);
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
		}
	}

	public int update(String collection, String type, int status, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {
		try {
			String updateSQL = "update " + tableName
					+ " set status=?, docSize=?, updateSize=?, deleteSize=?, isScheduled=?, startTime=?, endTime=?, duration=? "
					+ "where collection=? and type=?";
			PreparedStatement pstmt = conn.prepareStatement(updateSQL);
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
		}
	}

	public IndexingResult select(String collection, String type) {
		IndexingResult r = null;
		try {
			String selectSQL = "select collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
					+ tableName + " " + "where collection=? and type=?";
			PreparedStatement pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			ResultSet rs = pstmt.executeQuery();

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
		}

		return r;
	}

	public int delete(String collection, String type) {
		try {
			String deleteSQL = "delete from " + tableName + " where collection=? and type=?";
			PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			return pstmt.executeUpdate();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return 0;
	}

	// 최근업데이트건의 최근 시간만 가져온다.
	public Timestamp isUpdated(Timestamp lastTime) {
		try {
			String checkSQL = "select endTime from " + tableName + " " + "where endTime > ? " + "order by endTime desc";
			PreparedStatement pstmt = conn.prepareStatement(checkSQL);
			pstmt.setTimestamp(1, lastTime);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getTimestamp(1);
			}

			return null;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public int testAndCreate() throws SQLException {
		if (isExists() == false)
			create();

		try {
			conn = conn();
			pstmt = conn.prepareStatement("select collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
					+ tableName + " where collection = '0'");
			rs = pstmt.executeQuery();
			rs.next();
			return 0;
		} catch (SQLException e) {
			if (isExists()) {
				drop();
				create();
			}
			return 1;
		}
	}

	private void drop() {
		PreparedStatement pstmt = null;
		try {
			String insertSQL = "drop table " + tableName;
			pstmt = conn.prepareStatement(insertSQL);
			pstmt.executeUpdate();
			logger.info(insertSQL);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
		}

	}
}
