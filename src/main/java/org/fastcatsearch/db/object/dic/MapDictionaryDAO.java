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

package org.fastcatsearch.db.object.dic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.dao.BatchContext;
import org.fastcatsearch.db.object.DAOBase;
import org.fastcatsearch.db.vo.dic.MapDictionaryVO;

public class MapDictionaryDAO extends DAOBase {
	private int batchCount = 0;

	public MapDictionaryDAO(String tableName) {
		this.tableName = tableName;
	}

	public int create() throws SQLException {
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String createSQL = "create table " + tableName
					+ " (id int primary key,key varchar(30) not null unique,count int not null default 0,value varchar(255))";
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

	public int insert(String key, int count, String value) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String insertSQL = "insert into " + tableName + "(id, key, count, value) values (?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setString(parameterIndex++, key);
			pstmt.setInt(parameterIndex++, count);
			pstmt.setString(parameterIndex++, value);
			int c = pstmt.executeUpdate();
			if (c > 0) {
				ID++;
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

	public int update(String key, int count, String value) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String updateSQL = "UPDATE " + tableName + " SET count = ?,value = ? WHERE key = ?";
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, count);
			pstmt.setString(parameterIndex++, value);
			pstmt.setString(parameterIndex++, key);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public int delete(String key) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "delete from " + tableName + " where key = ?";
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, key);
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
			String countSQL = "SELECT count(*) FROM " + tableName + " where key=? or value like ?";
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

	public List<MapDictionaryVO> select(int startRow, int pageSize) {
		List<MapDictionaryVO> result = new ArrayList<MapDictionaryVO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		int totalCount = 0;
		try {
			conn = conn();
			try {
				conn = conn();
				String countSQL = "SELECT count(*) FROM " + tableName;
				Statement stmt = conn.createStatement();
				rs = stmt.executeQuery(countSQL);

				if (rs.next()) {
					totalCount = rs.getInt(1);
				}
				rs.close();
				stmt.close();

			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			} finally {
				releaseResource(pstmt, rs);
			}

			try {
				String selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName
						+ ") AS tmp WHERE rownum > ? and rownum <= ? order by id desc";

				pstmt = conn.prepareStatement(selectSQL);
				int parameterIndex = 1;
				pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
				pstmt.setInt(parameterIndex++, totalCount - startRow);
				rs = pstmt.executeQuery();
				logger.debug("Start = " + (totalCount - startRow - pageSize) + "~" + (totalCount - startRow));

				while (rs.next()) {
					MapDictionaryVO r = new MapDictionaryVO();
					parameterIndex = 2;
					r.id = rs.getInt(parameterIndex++);
					r.key = rs.getString(parameterIndex++);
					r.count = rs.getInt(parameterIndex++);
					r.value = rs.getString(parameterIndex++);
					result.add(r);
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			} finally {
				releaseResource(pstmt, rs);
				releaseConnection(conn);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}

		return result;
	}

	public List<MapDictionaryVO> selectWithKeyword(String keyword, int startRow, int pageSize) {
		List<MapDictionaryVO> result = new ArrayList<MapDictionaryVO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		int totalCount = 0;
		try {
			conn = conn();
			try {
				String countSQL = "SELECT count(*) FROM " + tableName + " where key=? or value like ?";
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
			}

			try {
				String selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName
						+ " where key=? or value like ?) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
				pstmt = conn.prepareStatement(selectSQL);
				int parameterIndex = 1;
				pstmt.setString(parameterIndex++, keyword);
				pstmt.setString(parameterIndex++, "%" + keyword + "%");
				pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
				pstmt.setInt(parameterIndex++, totalCount - startRow);
				rs = pstmt.executeQuery();

				while (rs.next()) {
					MapDictionaryVO r = new MapDictionaryVO();
					result.add(r);
					parameterIndex = 2;
					r.id = rs.getInt(parameterIndex++);
					r.key = rs.getString(parameterIndex++);
					r.count = rs.getInt(parameterIndex++);
					r.value = rs.getString(parameterIndex++);
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			} finally {
				releaseResource(pstmt, rs);
				releaseConnection(conn);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseConnection(conn);
		}

		return result;
	}

	public List<MapDictionaryVO> selectWithKeywordOnly(String keyword) {
		List<MapDictionaryVO> result = new ArrayList<MapDictionaryVO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String selectSQL = "SELECT * FROM " + tableName + " where key=?";
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				MapDictionaryVO r = new MapDictionaryVO();
				result.add(r);
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.key = rs.getString(parameterIndex++);
				r.count = rs.getInt(parameterIndex++);
				r.value = rs.getString(parameterIndex++);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}

		return result;
	}

	public int testAndCreate() throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			pstmt = conn.prepareStatement("select count(*) from " + tableName);
			rs = pstmt.executeQuery();
			rs.next();
			return 0;
		} catch (SQLException e) {
			create();
			return 1;
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
	}

	public BatchContext startInsertBatch() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			batchCount = 0;
			int inx = 0;
			pstmt = conn.prepareStatement("select max(id) from " + tableName);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				inx = rs.getInt(1);
				inx = inx + 1;
			}
			ID = inx;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
		}
		
		if(conn == null){
			//이전 작업이 실패하면 null 리턴.
			return null;
		}
		
		try{
			String insertSQL = "insert into " + tableName + "(id, key, count, value) values (?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			return new BatchContext(conn, pstmt);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean endInsertBatch(BatchContext batchContext) {
		Connection conn = batchContext.getConnection();
		PreparedStatement pstmt = batchContext.getPreparedStatement();
		try {
			int[] update_Count = pstmt.executeBatch();
			return true;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return false;
	}

	public int insertBatch(String line, BatchContext batchContext) {
		if (line.trim().length() == 0)
			return 0;

		Connection conn = batchContext.getConnection();
		PreparedStatement pstmt = batchContext.getPreparedStatement();
		try {
			batchCount = batchCount + 1;
			String[] kv = line.split(":");
			String[] s = kv[1].split(",");

			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setString(parameterIndex++, kv[0]);
			pstmt.setInt(parameterIndex++, s.length);
			pstmt.setString(parameterIndex++, kv[1]);

			pstmt.addBatch();
			ID = ID + 1;

			if ((batchCount % 1000) == 0)
				pstmt.executeBatch();

			return batchCount;

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		}
	}
}
