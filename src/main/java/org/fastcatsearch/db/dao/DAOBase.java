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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TreeSet;

import org.fastcatsearch.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DAOBase {

	protected static final Logger logger = LoggerFactory.getLogger(DAOBase.class);

	protected String tableName;
	protected ConnectionManager connectionManager;

	public DAOBase(String tableName) {
		this.tableName = tableName;
	}
	public DAOBase(ConnectionManager connectionManager) {
		tableName = this.getClass().getSimpleName();
		this.connectionManager = connectionManager;
	}
	public DAOBase(String tableName, ConnectionManager connectionManager) {
		this.tableName = tableName;
		this.connectionManager = connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public Connection conn() throws SQLException {
		return connectionManager.getConnection();
	}

	public void releaseResource(Object... objList) {
		if (objList == null) {
			return;
		}

		for (int i = 0; i < objList.length; i++) {
			Object obj = objList[i];
			if (obj == null) {
				continue;
			}

			if (obj instanceof Statement) {
				try {
					((Statement) obj).close();
				} catch (SQLException ignore) {
				}
			} else if (obj instanceof PreparedStatement) {
				try {
					((PreparedStatement) obj).close();
				} catch (SQLException ignore) {
				}
			} else if (obj instanceof ResultSet) {
				try {
					((ResultSet) obj).close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	// 릴리즈된 연결처리는 connectionManager에 위임한다.
	protected void releaseConnection(Connection conn) {
		connectionManager.releaseConnection(conn);
	}

	public boolean testAndCreate() throws SQLException {
		if (testTable()) {
			return true;
		}
		dropTable();
		createTable();
		return true;
	}

	public abstract boolean testTable();

	public abstract boolean createTable() throws SQLException;

	public boolean isExists() {
		if (connectionManager == null)
			return false;

		Connection conn = null;
		try {
			conn = conn();
			DatabaseMetaData dbmeta = conn.getMetaData();
			ResultSet rs = dbmeta.getTables(null, null, "%", null);
			TreeSet<String> ts = new TreeSet<String>();
			while (rs.next()) {
				ts.add(rs.getString(3).toLowerCase());
			}
			rs.close();
			return ts.contains(tableName.trim().toLowerCase());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			releaseConnection(conn);
		}
		return false;
	}

	public int dropTable() {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			String deleteSQL = "drop table " + tableName;
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

	public int truncate() {
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
		try {
			return selectInteger("SELECT count(*) FROM " + tableName);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return -1;
	}

	public int selectInteger(String selectQuery) throws SQLException {
		int value = -1;
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectQuery);

			if (rs.next()) {
				value = rs.getInt(1);
			}

		} finally {
			releaseResource(stmt, rs);
			releaseConnection(conn);
		}

		return value;
	}

	public int executeUpdate(String updateQuery, Object... params) throws SQLException {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = conn();
			pstmt = conn.prepareStatement(updateQuery);
			int parameterIndex = 1;
			for (int i = 0; i < params.length; i++) {
				Object param = params[i];
				if (param instanceof Integer) {
					pstmt.setInt(parameterIndex++, (Integer) param);
				} else if (param instanceof Float) {
					pstmt.setFloat(parameterIndex++, (Float) param);
				} else if (param instanceof Double) {
					pstmt.setDouble(parameterIndex++, (Double) param);
				} else if (param instanceof Long) {
					pstmt.setLong(parameterIndex++, (Long) param);
				} else if (param instanceof Timestamp) {
					pstmt.setTimestamp(parameterIndex++, (Timestamp) param);
				} else {
					pstmt.setString(parameterIndex++, (String) param);
				}
			}
			return pstmt.executeUpdate(updateQuery);
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public int executeUpdate(String updateQuery) throws SQLException {
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = conn();
			stmt = conn.createStatement();
			return stmt.executeUpdate(updateQuery);
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}

	public boolean testQuery(String testQuery) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = conn();
			pstmt = conn.prepareStatement(testQuery + " FETCH FIRST 10 ROWS ONLY");
			// OR where id = 0;
			rs = pstmt.executeQuery();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			releaseResource(rs, pstmt);
			releaseConnection(conn);
		}
	}

}
