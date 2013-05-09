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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SetDictionaryDAO extends DAOBase {
	public int id;
	public String term;
	public String tableName;
	public String fieldName;
	private int batchCount = 0;

	public SetDictionaryDAO() {
		tableName = getClass().getSimpleName();
		fieldName = "word";
	}

	public int create() throws SQLException {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String createSQL = "create table " + tableName + " (id int primary key," + fieldName
					+ " varchar(50) not null unique)";
			stmt = conn.createStatement();
			return stmt.executeUpdate(createSQL);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}

	public int insert(String customword) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			int inx = 0;
			pstmt = conn.prepareStatement("select max(id) from " + tableName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				inx = rs.getInt(1);
				inx = inx + 1;
			}

			String insertSQL = "insert into " + tableName + "(id, " + fieldName + ") values (?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, inx);
			pstmt.setString(parameterIndex++, customword);

			int c = pstmt.executeUpdate();
			if (c > 0) {
				ID = inx;
			}
			return c;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public PreparedStatement startInsertBatch() {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			batchCount = 0;
			int inx = 0;
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement("select max(id) from " + tableName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				inx = rs.getInt(1);
				inx = inx + 1;
			}
			ID = inx;

			String insertSQL = "insert into " + tableName + "(id, " + fieldName + ") values (?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			return pstmt;
		} catch (SQLException e) {
			return null;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public boolean endInsertBatch(PreparedStatement pstmt) {
		Connection conn = null;
		try {
			conn = conn();
			int[] update_Count = pstmt.executeBatch();
			conn.commit();
			pstmt.close();
			return true;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public int insertBatch(String customword, PreparedStatement pstmt) {

		Connection conn = null;
		try {
			conn = conn();
			batchCount = batchCount + 1;
			pstmt.clearParameters();
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setString(parameterIndex++, customword);
			pstmt.addBatch();
			ID = ID + 1;

			if ((batchCount % 1000) == 0)
				pstmt.executeBatch();

			return batchCount;

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseConnection(conn);
		}
	}

	public int selectCount() {
		int totalCount = 0;
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(*) FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);

			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(stmt, rs);
			releaseConnection(conn);
		}

		return totalCount;
	}

	public int selectWithKeywordCount(String keyword) {
		int totalCount = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(*) FROM " + tableName + " where " + fieldName + "=? or " + fieldName + " like ?";
			pstmt = conn.prepareStatement(countSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			pstmt.setString(parameterIndex++, "%" + keyword + "%");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}

		return totalCount;
	}

	public List<SetDictionaryDAO> select(int startRow, int pageSize) {
		List<SetDictionaryDAO> result = new ArrayList<SetDictionaryDAO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(*) FROM " + tableName;
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();

			String selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName
					+ ") AS tmp WHERE rownum > ? and rownum <= ? order by id desc";

			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
			logger.debug("Start = " + (totalCount - startRow - pageSize) + "~" + (totalCount - startRow));

			while (rs.next()) {
				UserDictionary r = new UserDictionary();
				parameterIndex = 2;
				r.id = rs.getInt(parameterIndex++);
				r.term = rs.getString(parameterIndex++);
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

	public List<SetDictionaryDAO> selectWithKeyword(String keyword, int startRow, int pageSize) {
		List<SetDictionaryDAO> result = new ArrayList<SetDictionaryDAO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(*) FROM " + tableName + " where " + fieldName + "=? or " + fieldName + " like ?";
			pstmt = conn.prepareStatement(countSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			pstmt.setString(parameterIndex++, "%" + keyword + "%");
			rs = pstmt.executeQuery();
			int totalCount = 0;
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			pstmt.close();

			String selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName
					+ " where " + fieldName + "=? or " + fieldName
					+ " like ?) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
			pstmt = conn.prepareStatement(selectSQL);
			parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			pstmt.setString(parameterIndex++, "%" + keyword + "%");
			pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				UserDictionary r = new UserDictionary();
				parameterIndex = 2;
				r.id = rs.getInt(parameterIndex++);
				r.term = rs.getString(parameterIndex++);
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

	public List<SetDictionaryDAO> selectWithKeywordOnly(String keyword) {
		List<SetDictionaryDAO> result = new ArrayList<SetDictionaryDAO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String selectSQL = "SELECT * FROM " + tableName + " where " + fieldName + "=?";
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				UserDictionary r = new UserDictionary();
				result.add(r);
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.term = rs.getString(parameterIndex++);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}

		return result;
	}

	public int delete(String customword) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "delete from " + tableName + " where " + fieldName + " = ?";
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, customword);

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public int deleteAll() {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "truncate table " + tableName;
			pstmt = conn.prepareStatement(deleteSQL);
			int count = pstmt.executeUpdate();
			return count;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public int testAndCreate() throws SQLException {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			pstmt = conn.prepareStatement("select count(*) from " + tableName);
			pstmt.executeQuery().next();
			return 0;
		} catch (SQLException e) {
			create();
			return 1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}
}
