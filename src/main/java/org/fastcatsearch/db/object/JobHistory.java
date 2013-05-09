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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JobHistory extends DAOBase{
	
	public long id;
	public long jobId;
	public String jobClassName;
	public String args;
	public boolean isSuccess;
	public String resultStr;
	public boolean isScheduled;
	public Timestamp startTime;
	public Timestamp endTime;
	public int duration;
	
	public JobHistory(){ }
	
	public int create() throws SQLException{
		
		Connection conn = null;
		Statement stmt = null;
		String createSQL = "create table " + tableName + "(id int primary key, jobId bigint, jobClassName varchar(200), args varchar(3000), isSuccess smallint, resultStr varchar(3000), isScheduled smallint, startTime timestamp, endTime timestamp, duration int)";
				
		try
		{
			conn = conn();
			stmt = conn.createStatement();
			return stmt.executeUpdate(createSQL);
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
		
	}
	
	public int insert(long jobId,
			String jobClassName, String args, boolean isSuccess, String resultStr, boolean isScheduled,
			Timestamp startTime, Timestamp endTime, int duration) {
		
		PreparedStatement pstmt = null;
		Connection conn = null;
		
		try{
			conn = conn();
			
			String insertSQL = "insert into " + tableName + "(id, jobId, jobClassName, args, isSuccess, resultStr, isScheduled, startTime, endTime, duration) values (?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setLong(parameterIndex++, ID);
			pstmt.setLong(parameterIndex++, jobId);
			pstmt.setString(parameterIndex++, jobClassName);
			pstmt.setString(parameterIndex++, args);
			pstmt.setBoolean(parameterIndex++, isSuccess);
			pstmt.setString(parameterIndex++, resultStr);
			pstmt.setBoolean(parameterIndex++, isScheduled);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setTimestamp(parameterIndex++, endTime);
			pstmt.setInt(parameterIndex++, duration);
			int c =  pstmt.executeUpdate();
			if(c > 0){
				ID++;
			}
			return c;
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			return -1;
		}finally{
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	public int count() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = conn();
			String countSQL = "SELECT count(id) FROM " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if(rs.next()){
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		
			return totalCount;
			
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
			return 0;
		} finally {
			releaseResource(stmt, rs);
			releaseConnection(conn);
		}
	}
	
	public List<JobHistory> select(int startRow, int length) {
		String countSQL = "SELECT count(id) FROM " + tableName;
		String selectSQL = "SELECT id, jobId, jobClassName, args, isSuccess, resultStr, isScheduled, startTime, endTime, duration" +
				" FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName + " ) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
				
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<JobHistory> result = new ArrayList<JobHistory>();
		
		try {
			conn = conn();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if(rs.next()){
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
			logger.debug("Start = "+(totalCount - length)+ "~"+(totalCount - startRow));
			while(rs.next()){
				JobHistory r = new JobHistory();
				
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.jobId = rs.getLong(parameterIndex++);
				r.jobClassName = rs.getString(parameterIndex++);
				r.args = rs.getString(parameterIndex++);
				r.isSuccess = rs.getBoolean(parameterIndex++);
				r.resultStr = rs.getString(parameterIndex++);
				r.isScheduled = rs.getBoolean(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.endTime = rs.getTimestamp(parameterIndex++);
				r.duration = rs.getInt(parameterIndex++);
				
				result.add(r);
			}
			
			pstmt.close();
			rs.close();
			
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(stmt, pstmt, rs);
			releaseConnection(conn);
		}
		
		return result;
	}
	
	public int testAndCreate() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
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
}
