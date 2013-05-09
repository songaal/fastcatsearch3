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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.vo.SystemMonitoringInfoMinuteVO;

public class SystemMonitoringInfoMinute extends DAOBase {
	
	public SystemMonitoringInfoMinute(ConnectionManager connectionManager) {
		super(connectionManager);
	}
	
	@Override
	public boolean testTable() {
		return testQuery("select id, cpu, mem, load, when from " + tableName);
	}
	
	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table "+tableName+"(id int GENERATED ALWAYS AS IDENTITY primary key, cpu int, mem int, load double, when timestamp)";
		executeUpdate(createSQL);
		return true;
	}
	
	public int insert(int cpu, int mem, double load, Timestamp when) {
		String insertSQL = "insert into "+tableName+"(cpu, mem, load, when) values (?,?,?,?)";

		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = conn();
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, cpu);
			pstmt.setInt(parameterIndex++, mem);
			pstmt.setDouble(parameterIndex++, load);
			pstmt.setTimestamp(parameterIndex++, when);
			int c =  pstmt.executeUpdate();
			return c;
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			return -1;
		}finally{
			releaseResource(pstmt);
			releaseConnection(conn);
		}
	}

	
	public List<SystemMonitoringInfoMinuteVO> select(int startRow, int length) {
		String selectSQL = "SELECT id, cpu, mem, load, when" +
				" FROM "+tableName+" WHERE id > ? and id <= ? order by id desc";
		
		Connection conn = null;
		ResultSet rs = null;	
		PreparedStatement pstmt = null;
		
		List<SystemMonitoringInfoMinuteVO> result = new ArrayList<SystemMonitoringInfoMinuteVO>();
		
		try{
			int totalCount = selectCount();
			conn = conn();
			
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
			logger.debug("Start = "+(totalCount - length)+ "~"+(totalCount - startRow));
			while(rs.next()){
				SystemMonitoringInfoMinuteVO r = new SystemMonitoringInfoMinuteVO();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.cpu = rs.getInt(parameterIndex++);
				r.mem = rs.getInt(parameterIndex++);
				r.load = rs.getDouble(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				
				result.add(r);
			}
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
		
		return result;
	}
	
	public List<SystemMonitoringInfoMinuteVO> select(Timestamp start, Timestamp end) {
		List<SystemMonitoringInfoMinuteVO> result = new ArrayList<SystemMonitoringInfoMinuteVO>();
		String selectSQL = "SELECT id, cpu, mem, load, when" +
				" FROM "+tableName+" WHERE when >= ? and when <= ? order by id desc";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
			conn = conn();
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setTimestamp(parameterIndex++, start);
			pstmt.setTimestamp(parameterIndex++, end);
			rs = pstmt.executeQuery();
			logger.debug("Start = "+start+ "~"+end);
			while(rs.next()){
				SystemMonitoringInfoMinuteVO r = new SystemMonitoringInfoMinuteVO();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.cpu = rs.getInt(parameterIndex++);
				r.mem = rs.getInt(parameterIndex++);
				r.load = rs.getDouble(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				
				result.add(r);
			}
			
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
		
		return result;
	}
	
	public int deleteOld(int month) {
		String deleteSQL = "Delete From "+tableName+" Where when < ?";
		Calendar oldDatetime = Calendar.getInstance();
		oldDatetime.set(Calendar.SECOND, 0);
		oldDatetime.set(Calendar.MINUTE, 0);
		oldDatetime.set(Calendar.HOUR, 0);
		oldDatetime.add(Calendar.MONTH, -month);
		try {
			return executeUpdate(deleteSQL, new Timestamp(oldDatetime.getTimeInMillis()));
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return -1;
		}
	}
	
}