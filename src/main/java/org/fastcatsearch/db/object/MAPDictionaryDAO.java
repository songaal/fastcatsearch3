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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MAPDictionaryDAO extends DAOBase {
	public int id;
	public String dickey;
	public int count;
	public String value;
	private int batchCount=0;

	public MAPDictionaryDAO() { }

	public int create() throws SQLException {
			Statement stmt = null;
		try {
			String createSQL = "create table " + tableName + " (id int primary key,dickey varchar(30) not null unique,count int not null default 0,value varchar(255))";
			stmt = conn.createStatement();
			return stmt.executeUpdate(createSQL);
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return -1;
		} finally {
			try {
				if(stmt != null)
					stmt.close();
			} catch (SQLException e) {
			}
		}
	}

	public int insert(String dickey, int count, String value) {
			PreparedStatement pstmt = null;
		try {
			String insertSQL = "insert into " + tableName + "(id, dickey, count, value) values (?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setString(parameterIndex++, dickey);
			pstmt.setInt(parameterIndex++, count);
			pstmt.setString(parameterIndex++, value);
			int c =  pstmt.executeUpdate();
			if(c > 0){
				ID++;
			}
			return c;
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return -1;
		} finally {
			try {
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}
	}

	public int update(String dickey, int count, String value) {
			PreparedStatement pstmt = null;
		try {
			String updateSQL = "UPDATE " + tableName + " SET count = ?,value = ? WHERE dickey = ?";
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, count);
			pstmt.setString(parameterIndex++, value);
			pstmt.setString(parameterIndex++, dickey);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return -1;
		} finally {
			try {
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}
	}

	public int delete(String word) {
			PreparedStatement pstmt = null;
		try {
			String deleteSQL = "delete from " + tableName + " where dickey = ?";
			pstmt = conn.prepareStatement(deleteSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, word);
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return -1;
		} finally {
			try {
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}
	}
	
	public int deleteAll() {
		PreparedStatement pstmt = null;
		try {
			String deleteSQL = "truncate table " + tableName;
			pstmt = conn.prepareStatement(deleteSQL);
			int count = pstmt.executeUpdate();
			return count;
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return -1;
		} finally {
			if(pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
		}
	}
	
	public int selectCount() {
			int totalCount = 0;
			Statement stmt = null;
			ResultSet rs = null;
		try {
			String countSQL = "SELECT count(*) FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);

			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(stmt != null)
					stmt.close();
			} catch (SQLException e) {
			}
		}

		return totalCount;
	}

	public int selectWithKeywordCount(String keyword) {
			int totalCount = 0;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
		try {
			String countSQL = "SELECT count(*) FROM " + tableName + " where dickey=? or value like ?";
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
			logger.error(e.getMessage(),e);
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}

		return totalCount;
	}

	public List<MAPDictionaryDAO> select(int startRow,int pageSize) {
			List<MAPDictionaryDAO> result = new ArrayList<MAPDictionaryDAO>();
			PreparedStatement pstmt = null;
			ResultSet rs = null;
		try {
			String countSQL = "SELECT count(*) FROM " + tableName;
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if (rs.next()) {
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();

			String selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName + ") AS tmp WHERE rownum > ? and rownum <= ? order by id desc";

			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
			logger.debug("Start = " + (totalCount - startRow - pageSize) + "~" + (totalCount - startRow));

			while (rs.next()) {
				MAPDictionaryDAO r = new MAPDictionaryDAO();
				parameterIndex = 2;
				r.id = rs.getInt(parameterIndex++);
				r.dickey = rs.getString(parameterIndex++);
				r.count = rs.getInt(parameterIndex++);
				r.value = rs.getString(parameterIndex++);
				result.add(r);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}

		return result;
	}

	public List<MAPDictionaryDAO> selectWithKeyword(String keyword, int startRow, int pageSize) {
			List<MAPDictionaryDAO> result = new ArrayList<MAPDictionaryDAO>();
			PreparedStatement pstmt = null;
			ResultSet rs = null;
		try {
			String countSQL = "SELECT count(*) FROM " + tableName + " where dickey=? or value like ?";
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
			String selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName + " where dickey=? or value like ?) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
			pstmt = conn.prepareStatement(selectSQL);
			parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			pstmt.setString(parameterIndex++, "%" + keyword + "%");
			pstmt.setInt(parameterIndex++, totalCount - startRow - pageSize);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				MAPDictionaryDAO r = new MAPDictionaryDAO();
				result.add(r);
				parameterIndex = 2;
				r.id = rs.getInt(parameterIndex++);
				r.dickey = rs.getString(parameterIndex++);
				r.count = rs.getInt(parameterIndex++);
				r.value = rs.getString(parameterIndex++);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}

		return result;
	}

	public List<MAPDictionaryDAO> selectWithKeywordOnly(String keyword) {
			List<MAPDictionaryDAO> result = new ArrayList<MAPDictionaryDAO>();
			PreparedStatement pstmt = null;
			ResultSet rs = null;
		try {
			String selectSQL = "SELECT * FROM " + tableName + " where dickey=?";
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				MAPDictionaryDAO r = new MAPDictionaryDAO();
				result.add(r);
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.dickey = rs.getString(parameterIndex++);
				r.count = rs.getInt(parameterIndex++);
				r.value = rs.getString(parameterIndex++);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}

		return result;
	}
	public int testAndCreate() throws SQLException {
		try {
			conn.prepareStatement("select count(*) from " + tableName).executeQuery().next();
			return 0;
		} catch (SQLException e) {
			create();
			return 1;
		}
	}
	
	public PreparedStatement startInsertBatch() {
		try {
			batchCount = 0;
			int inx = 0;
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement("select max(id) from "+tableName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				inx = rs.getInt(1);
				inx = inx + 1;
			}
			ID = inx;

			String insertSQL = "insert into "+tableName+"(id, dickey, count, value) values (?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			return pstmt;
		} catch (SQLException e) {
			return null;
		}
	}

	public boolean endInsertBatch(PreparedStatement pstmt) {
		int[] update_Count = { 0, };
		try {
			update_Count = pstmt.executeBatch();
			conn.commit();
			pstmt.close();
			return true;
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;
	}

	public int insertBatch(String line, PreparedStatement pstmt) {
		if ( line.trim().length() == 0 )
			return 0;

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
