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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.vo.SetDictionaryVO;

public class SetDictionaryDAO extends DAOBase implements ResultVOMapper<SetDictionaryVO> {

	public SetDictionaryDAO(String tableName, ConnectionManager connectionManager) {
		super(tableName, connectionManager);
	}

	@Override
	public boolean testTable() {
		return testQuery("select id, keyword from " + tableName);
	}

	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table " + tableName
				+ " (id int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, keyword varchar(2000) not null )";
		executeUpdate(createSQL);
		return true;
	}

	public int insert(String customword) {
		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			String insertSQL = "insert into " + tableName + "(keyword) values (?)";
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

	public int update(int id, String keyword){
		String updateSQL = "update into " + tableName + " set keyword = ? where id = ?";
		try {
			return executeUpdate(updateSQL, keyword, id);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return -1;
		}
	}
	
	public BatchContext startInsertBatch() {
		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			String insertSQL = "insert into " + tableName + "(keyword) values (?)";
			conn = conn();
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
			batchContext.close();
			return -1;
		}
	}

	public int selectCountWithKeyword(String keyword) {
		if(keyword == null || keyword.length() == 0){
			return selectCount();
		}
		int totalCount = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			String countSQL = "SELECT count(*) FROM " + tableName + " where keyword like ?";
			pstmt = conn.prepareStatement(countSQL);
			int parameterIndex = 1;
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
				selectSQL = "SELECT id, keyword FROM " + tableName;
				if (keyword != null) {
					if (isExactMatch) {
						selectSQL += " where keyword = ?";
					} else {
						selectSQL += " where keyword like ?";
					}
				}
			} else {
				// 페이징시만 총 갯수가져옴.
				if (keyword != null) {
					totalCount = selectCountWithKeyword(keyword);
				} else {
					totalCount = selectCount();
				}

				selectSQL = "SELECT id, keyword FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName;

				if (keyword != null) {
					if (isExactMatch) {
						selectSQL += " where keyword = ?";
					} else {
						selectSQL += " where keyword like ?";
					}
				}

				selectSQL += ") AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
			}

			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			if (keyword != null) {
				if (isExactMatch) {
					pstmt.setString(parameterIndex++, keyword);
				}else{
					pstmt.setString(parameterIndex++, "%" + keyword + "%");
				}
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
			releaseConnection(conn);
		}

		return result;
	}

	public int delete(String customword) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "delete from " + tableName + " where keyword = ?";
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
	
	public int deleteById(int id) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "delete from " + tableName + " where id = ?";
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
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

	@Override
	public SetDictionaryVO map(ResultSet resultSet) throws SQLException {
		SetDictionaryVO vo = new SetDictionaryVO();
		int index = 1;
		vo.id = resultSet.getInt(index++);
		vo.keyword = resultSet.getString(index++);
		return vo;
	}
}
