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

import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.vo.IndexingResultVO;
import org.fastcatsearch.ir.common.IndexingType;

public class IndexingResult extends DAOBase {

//	public final static String TYPE_NONE_INDEXING = "-";
//	public final static String TYPE_FULL_INDEXING = "F";
//	public final static String TYPE_INC_INDEXING = "I";
	
	public final static int STATUS_FAIL = -1;
	public final static int STATUS_SUCCESS = 0;
	public final static int STATUS_RUNNING = 1;

	public IndexingResult(ConnectionManager connectionManager) {
		super(connectionManager);
	}

	@Override
	public boolean testTable() {
		return testQuery("select collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
				+ tableName + " where collection = '0'");
	}

	public boolean createTable() throws SQLException {
		String createSQL = "create table "
				+ tableName
				+ "(collection varchar(20), type char(4), status smallint, docSize int, updateSize int, deleteSize int, isScheduled smallint, startTime timestamp, endTime timestamp, duration int" +
				",PRIMARY KEY (collection, type))";

		executeUpdate(createSQL);
		return true;
	}

	public synchronized int repairStatus() throws SQLException {
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

	public int updateOrInsert(String collection, IndexingType type, int status, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {

		String checkSQL = "select count(collection) from " + tableName + " where collection=? and type=?";

		try {
			int count = selectInteger(checkSQL, collection, type.name());
			
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

	public synchronized int insert(String collection, IndexingType type, int status, int docSize, int updateSize, int deleteSize,
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
			pstmt.setString(parameterIndex++, type.name());
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

	public synchronized int update(String collection, IndexingType type, int status, int docSize, int updateSize, int deleteSize,
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
			pstmt.setString(parameterIndex++, type.name());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public synchronized int updateResult(String collection, IndexingType type, int status, int docSize, int updateSize, int deleteSize,
			Timestamp endTime, int duration) {
		String updateSQL = "update " + tableName
				+ " set status=?, docSize=?, updateSize=?, deleteSize=?, endTime=?, duration=? "
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
			pstmt.setTimestamp(parameterIndex++, endTime);
			pstmt.setInt(parameterIndex++, duration);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type.name());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}
	
	public IndexingResultVO select(String collection, String type) {
		String selectSQL = "select collection, type, status, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
				+ tableName + " " + "where collection=? and type=?";

		IndexingResultVO r = null;
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
				r = new IndexingResultVO();

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

	public synchronized int delete(String collection, IndexingType type) {
		String deleteSQL = "delete from " + tableName + " where collection=? and type=?";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = conn();
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type.name());
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
