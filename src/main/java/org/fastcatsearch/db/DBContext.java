package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBContext {
	Connection conn;
	ResultSet rs;

	ConnectionManager connectionManager;

	public void close() {
		connectionManager.releaseConnection(conn);
	}

	// db
	public int updateOrInsertSQL(String sql) throws SQLException {
		Connection conn = getConn();
		try {
			Statement stmt = conn.createStatement();
			int n = stmt.executeUpdate(sql);
			return n;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public ResultSet selectSQL(String sql) throws SQLException {
		Connection conn = getConn();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			return rs;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
