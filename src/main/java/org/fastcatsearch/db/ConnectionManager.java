package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	
	private String jdbcUrl;
	private String user;
	private String password;
	
	public ConnectionManager(String jdbcUrl){
		this(jdbcUrl, null, null);
	}
	
	public ConnectionManager(String jdbcUrl, String user, String password){
		this.jdbcUrl = jdbcUrl;
		this.user = user;
		this.password = password;
	}
	
	public Connection getConnection() throws SQLException{
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(jdbcUrl, user, password);
		} catch (SQLException e) {
			// if DB is not created.
			conn = createDB(jdbcUrl, null, null);
			if (conn == null) {
				throw e;
			}
		}
		
		return conn;
	}
	
	private Connection createDB(String jdbcurl, String jdbcuser, String jdbcpass) {
		try {
			if (jdbcuser != null && jdbcpass != null) {
				return DriverManager.getConnection(jdbcurl + ";create=true", user, password);
			} else {
				return DriverManager.getConnection(jdbcurl + ";create=true");
			}
		} catch (SQLException e) {

		}
		return null;
	}

	public void close() throws SQLException {
		
	}

	public void releaseConnection(Connection conn) {
		try {
			if(conn != null){
				conn.close();
			}
		} catch (SQLException ignore) {
		}		
	}
}
