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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SystemMonitoringInfo extends DAOBase {
	
	public int id;
	public int cpu;
	public int mem;
	public double load;
	public Timestamp when;
	public String type;
	
	public SystemMonitoringInfo(){ }
	
	//type:h 시간, d 일, w 주, m 월, y 년
	
	@Override
	public boolean testTable() {
		return testQuery("select id, collection, hit, fail, achit, acfail, ave_time, max_time, when, type from " + tableName);
	}
	
	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table "+tableName+"(id int GENERATED ALWAYS AS IDENTITY primary key, cpu int, mem int, load double, when timestamp, type varchar(1))";
		executeUpdate(createSQL);
		return true;
	}
	
	public int insert(int cpu, int mem, double load, Timestamp when, String type) {
		String insertSQL = "insert into "+tableName+"(cpu, mem, load, when, type) values (?,?,?,?,?)";
				
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
			pstmt.setString(parameterIndex++, type);
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

	
	public List<SystemMonitoringInfo> select(int startRow, int length) {
		List<SystemMonitoringInfo> result = new ArrayList<SystemMonitoringInfo>();
		String selectSQL = "SELECT id, cpu, mem, load, when, type" +
				" FROM "+tableName+" WHERE id > ? and id <= ? order by id desc";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			int totalCount = selectCount();
			
			conn = conn();
		
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
//			logger.debug("Start = "+(totalCount - length)+ "~"+(totalCount - startRow));
			while(rs.next()){
				SystemMonitoringInfo r = new SystemMonitoringInfo();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.cpu = rs.getInt(parameterIndex++);
				r.mem = rs.getInt(parameterIndex++);
				r.load = rs.getDouble(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
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
	
	public List<SystemMonitoringInfo> select(Timestamp start, Timestamp end, String type) {
		String selectSQL = "SELECT id, cpu, mem, load, when, type" +
				" FROM "+tableName+" WHERE when >= ? and when <= ? and type = ? order by id desc";
		List<SystemMonitoringInfo> result = new ArrayList<SystemMonitoringInfo>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
			conn = conn();
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setTimestamp(parameterIndex++, start);
			pstmt.setTimestamp(parameterIndex++, end);
			pstmt.setString(parameterIndex++, type);
			rs = pstmt.executeQuery();
			logger.debug("Start = "+start+ "~"+end);
			while(rs.next()){
				SystemMonitoringInfo r = new SystemMonitoringInfo();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.cpu = rs.getInt(parameterIndex++);
				r.mem = rs.getInt(parameterIndex++);
				r.load = rs.getDouble(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				result.add(r);
			}
			
			pstmt.close();
			rs.close();
			
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
		return result;
	}

	public int deleteOld(int month) {
		String deleteSQL = "Delete From "+tableName+" Where when < ?";
				
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			conn = conn();
			pstmt = conn.prepareStatement(deleteSQL);
			Calendar oldDatetime = Calendar.getInstance();
			oldDatetime.set(Calendar.SECOND, 0);
			oldDatetime.set(Calendar.MINUTE, 0);
			oldDatetime.set(Calendar.HOUR, 0);
			oldDatetime.add(Calendar.MONTH, -month);
			pstmt.setTimestamp(1, new Timestamp(oldDatetime.getTimeInMillis()));
			return pstmt.executeUpdate();
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(pstmt);
			releaseConnection(conn);
		}
		return -1;
	}
}

