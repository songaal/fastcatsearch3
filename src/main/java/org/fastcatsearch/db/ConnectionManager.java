package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	
	String jdbcUrl;
	String user;
	String password;
	
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
				throw new SQLException("내부 DB로의 연결을 생성할수 없습니다. DB를 이미 사용중인 프로세스가 있는지 확인필요.");
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
