package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB서비스에 DAO로 등록되지 않은 단편적 SQL문등을 실행하고자 할때 이 객체를 기반으로 사용한다. 
 * 시작시 connection객체가 생성되며, close시 사용된 모든 리소스가 자동으로 닫힌다. 
 * */
public class DBContext {

	protected static Logger logger = LoggerFactory.getLogger(DBContext.class);

	private Connection connection;
	private ConnectionManager connectionManager;

	private ResultSet resultSet;
	private Statement statement;

	public DBContext(ConnectionManager connectionManager) throws SQLException {
		this.connectionManager = connectionManager;
		connection = connectionManager.getConnection();
	}
	
	public int updateSQL(String sql) throws SQLException {
		statement = connection.createStatement();
		return statement.executeUpdate(sql);
	}

	public ResultSet selectSQL(String sql) throws SQLException {
		ensurePreviousResourceClosed();
		
		statement = connection.createStatement();
		resultSet = statement.executeQuery(sql);
		
		return resultSet;
	}
	
	//연속적으로 사용할때 이전리소스를 닫아준다. 
	private void ensurePreviousResourceClosed(){
		try {
			if(resultSet != null){
				resultSet.close();
				resultSet = null;
			}
		} catch (SQLException ignore) {
		}
		try {
			if(statement != null){
				statement.close();
				statement = null;
			}
		} catch (SQLException ignore) {
		}
	}
	
	public void close() {
		ensurePreviousResourceClosed();
		connectionManager.releaseConnection(connection);
	}

}
