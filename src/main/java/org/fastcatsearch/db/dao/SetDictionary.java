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
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.vo.SetDictionaryVO;

public class SetDictionary extends DAOBase implements ResultVOMapper<SetDictionaryVO> {

	public SetDictionary(ConnectionManager connectionManager) {
		super(connectionManager);
	}

	public SetDictionary(String tableName, ConnectionManager connectionManager) {
		super(connectionManager);
		this.tableName = tableName;
	}

	@Override
	public boolean testTable() {
		return testQuery("select id, key from " + tableName);
	}

	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table " + tableName
				+ " (id int GENERATED ALWAYS AS IDENTITY primary key , key varchar(50) not null unique)";
		executeUpdate(createSQL);
		return true;
	}

	public int insert(String customword) {
		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			String insertSQL = "insert into " + tableName + "(key) values (?)";
			conn = conn();
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, customword);

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

	public PreparedStatement startInsertBatch() {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			String insertSQL = "insert into " + tableName + "(key) values (?)";
			conn = conn();
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

	public int insertBatch(String keyword, BatchContext batchContext) {
		if (keyword.trim().length() == 0)
			return 0;

		PreparedStatement pstmt = batchContext.getPreparedStatement();
		try {
			pstmt.clearParameters();
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			pstmt.addBatch();

			int batchCount = batchContext.incrementBatchCountAndGet();
			if ((batchCount % 1000) == 0)
				pstmt.executeBatch();

			return batchCount;

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		}
	}

	public int selectCountWithKeyword(String keyword) {
		int totalCount = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(*) FROM " + tableName + " where key = ? or key like ?";
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

	public List<SetDictionaryVO> selectPage(int startRow, int pageSize) {
		return selectPageWithKeyword(null, startRow, pageSize);
	}

	public List<SetDictionaryVO> selectPageWithKeyword(String keyword, int startRow, int pageSize) {
		return selectPageWithKeyword(keyword, false, startRow, pageSize);
	}

	public List<SetDictionaryVO> selectWithExactKeyword(String keyword) {
		return selectPageWithKeyword(keyword, true, -1, -1);
	}

	public List<SetDictionaryVO> selectPageWithKeyword(String keyword, boolean isExactMatch, int startRow, int pageSize) {
		List<SetDictionaryVO> result = new ArrayList<SetDictionaryVO>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;

		try {
			int totalCount = 0;

			conn = conn();
			String selectSQL = null;
			boolean noPaging = (startRow == -1 && pageSize == -1);

			if (noPaging) {
				// 페이징없음.
				selectSQL = "SELECT * FROM " + tableName;
				if (keyword != null) {
					if (isExactMatch) {
						selectSQL += " where key = ?";
					} else {
						selectSQL += " where key = ? or key like ?";
					}
				}
			} else {
				// 페이징시만 총 갯수가져옴.
				if (keyword != null) {
					totalCount = selectCountWithKeyword(keyword);
				} else {
					totalCount = selectCount();
				}

				selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName;

				if (keyword != null) {
					selectSQL += (" where key = ? or key like ?");
				}

				selectSQL += ") AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
			}

			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			if (keyword != null) {
				pstmt.setString(parameterIndex++, keyword);
				pstmt.setString(parameterIndex++, "%" + keyword + "%");
			}
			if (!noPaging) {
				pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
				pstmt.setInt(parameterIndex++, totalCount - startRow);
			}
			rs = pstmt.executeQuery();

			while (rs.next()) {
				result.add(map(rs));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
		}

		return result;
	}

	public int delete(String customword) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "delete from " + tableName + " where key = ?";
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

	@Override
	public SetDictionaryVO map(ResultSet resultSet) throws SQLException {
		return map(resultSet, 1);
	}

	@Override
	public SetDictionaryVO map(ResultSet resultSet, int index) throws SQLException {
		SetDictionaryVO vo = new SetDictionaryVO();
		vo.id = resultSet.getInt(index++);
		vo.key = resultSet.getString(index++);
		return vo;
	}
}