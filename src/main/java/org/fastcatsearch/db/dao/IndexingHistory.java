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
import org.fastcatsearch.db.vo.IndexingHistoryVO;

public class IndexingHistory extends DAOBase {

	public IndexingHistory(ConnectionManager connectionManager) {
		super(connectionManager);
	}

	@Override
	public boolean testTable() {
		return testQuery("select id, collection, type, isSuccess, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from "
				+ tableName);
	}
	
	@Override
	public boolean createTable() throws SQLException {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String createSQL = "create table "
					+ tableName
					+ "(id int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) primary key, collection varchar(20), type char(1), isSuccess smallint, docSize int, updateSize int, deleteSize int, isScheduled smallint, startTime timestamp, endTime timestamp, duration int)";
			stmt = conn.createStatement();
			stmt.executeUpdate(createSQL);
			return true;
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}

	public int insert(String collection, String type, boolean isSuccess, int docSize, int updateSize, int deleteSize,
			boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String insertSQL = "insert into "
					+ tableName
					+ "(collection, type, isSuccess, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration) values (?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			pstmt.setBoolean(parameterIndex++, isSuccess);
			pstmt.setInt(parameterIndex++, docSize);
			pstmt.setInt(parameterIndex++, updateSize);
			pstmt.setInt(parameterIndex++, deleteSize);
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

	public int count() {
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(id) FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();

			return totalCount;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return 0;
		} finally {
			releaseResource(stmt, rs);
			releaseConnection(conn);
		}

	}

	public List<IndexingHistoryVO> select(int startRow, int length) {
		List<IndexingHistoryVO> result = new ArrayList<IndexingHistoryVO>();
		ResultSet rs = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		int totalCount = 0;
		try {
			conn = conn();
			String countSQL = "SELECT count(id) FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return result;
		}finally{
			releaseResource(stmt, rs);
			releaseConnection(conn);
		}
		
		try{
			conn = conn();
			if (totalCount - startRow <= 0)
				return result;

			String selectSQL = "SELECT id, collection, type, isSuccess, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration"
					+ " FROM ( SELECT ROW_NUMBER() OVER() AS rownum, "
					+ tableName
					+ ".* FROM "
					+ tableName
					+ " ) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
			// logger.debug("totalCount = "+totalCount+", startRow="+startRow+", Start = "+(totalCount - startRow - length)+
			// "~"+(totalCount - startRow));
			while (rs.next()) {
				IndexingHistoryVO r = new IndexingHistoryVO();

				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.isSuccess = rs.getBoolean(parameterIndex++);
				r.docSize = rs.getInt(parameterIndex++);
				r.updateSize = rs.getInt(parameterIndex++);
				r.deleteSize = rs.getInt(parameterIndex++);
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


	private void drop() {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String insertSQL = "drop table " + tableName;
			pstmt = conn.prepareStatement(insertSQL);
			pstmt.executeUpdate();
			logger.info(insertSQL);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}

	}
}
