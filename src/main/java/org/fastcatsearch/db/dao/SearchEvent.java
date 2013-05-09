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
import org.fastcatsearch.db.vo.SearchEventVO;

public class SearchEvent extends DAOBase {

	public SearchEvent(ConnectionManager connectionManager) {
		super(connectionManager);
	}

	@Override
	public boolean testTable() {
		return testQuery("select id, when, type, category, summary, stacktrace, status from " + tableName);
	}

	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table "
				+ tableName
				+ "(id int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) primary key, when timestamp, type char(5), category int, summary varchar(200), stacktrace varchar(3000), status char(1))";
		executeUpdate(createSQL);
		return true;
	}

	public int insert(Timestamp when, String type, int category, String summary, String stacktrace, String status) {
		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = conn();
			String insertSQL = "insert into " + tableName
					+ "(when, type, category, summary, stacktrace, status) values (?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setTimestamp(parameterIndex++, when);
			pstmt.setString(parameterIndex++, type);
			pstmt.setInt(parameterIndex++, category);
			pstmt.setString(parameterIndex++, summary);
			pstmt.setString(parameterIndex++, stacktrace);
			pstmt.setString(parameterIndex++, status);
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

	public List<SearchEventVO> select(int startRow, int length) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;

		List<SearchEventVO> result = new ArrayList<SearchEventVO>();
		try {

			int totalCount = selectCount();

			//
			// it occurs id offset bug when record erased, so use rownum insted of id
			//
			// String selectSQL = "SELECT id, when, type, category, summary, stacktrace, status" +
			// " FROM SearchEvent WHERE id > ? and id <= ? order by id desc";
			conn = conn();
			String selectSQL = "select * from (select id, when, type, category, summary, "
					+ "stacktrace, status, row_number() over () as rownum from SearchEvent) "
					+ "SearchEvent where rownum > ? and rownum <= ? order by id desc";
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
			// logger.debug("Start = {} ~ {}", (totalCount - length), (totalCount - startRow));
			while (rs.next()) {
				SearchEventVO r = new SearchEventVO();

				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.category = rs.getInt(parameterIndex++);
				r.summary = rs.getString(parameterIndex++);
				r.stacktrace = rs.getString(parameterIndex++);
				r.status = rs.getString(parameterIndex++);

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

	// 최근업데이트건의 최근 시간만 가져온다.
	public Timestamp isUpdated(Timestamp lastTime) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String checkSQL = "select when from SearchEvent " + "where when > ? " + "order by when desc";

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
			releaseResource(rs, pstmt);
			releaseConnection(conn);
		}
	}

	public int update(int id, String status) {
		logger.debug("id={}, status={}", id, status);
		Connection conn = null;
		PreparedStatement pstmt = null;
		String updateSQL = "update SearchEvent set status=?" + "where id=?";

		try {
			conn = conn();
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, status.toUpperCase());
			pstmt.setInt(parameterIndex++, id);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}
}
