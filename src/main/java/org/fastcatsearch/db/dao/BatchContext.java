package org.fastcatsearch.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BatchContext {
	private Connection conn;
	private PreparedStatement pstmt;
	private int batchCount;

	public BatchContext(Connection conn, PreparedStatement pstmt) {
		this.conn = conn;
		this.pstmt = pstmt;
	}
	
	public Connection getConnection() {
		return conn;
	}

	public PreparedStatement getPreparedStatement() {
		return pstmt;
	}
	
	public int getBatchCount(){
		return batchCount;
	}
	
	public int incrementBatchCountAndGet(){
		return ++batchCount;
	}

	
}
