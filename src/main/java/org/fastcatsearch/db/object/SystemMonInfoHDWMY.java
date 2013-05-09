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
import java.util.Calendar;
import java.util.List;

import org.fastcatsearch.db.object.DAOBase;

public class SystemMonInfoHDWMY extends DAOBase {
	
	public int id;
	public int cpu;
	public int mem;
	public double load;
	public Timestamp when;
	public String type;
	
	public SystemMonInfoHDWMY(){ }
	
	//type:h 시간, d 일, w 주, m 월, y 년
	
	public int create() throws SQLException{
		Connection conn = null;
		Statement stmt = null;
		String createSQL = "create table "+tableName+"(id int primary key, cpu int, mem int, load double, when timestamp, type varchar(1))";
		
		try {
			conn = conn();
			stmt = conn.createStatement();
			return stmt.executeUpdate(createSQL);
		} finally {
			releaseResource(stmt);
			releaseConnection(conn);
		}
	}
	
	public int insert(int cpu, int mem, double load, Timestamp when, String type) {
		String insertSQL = "insert into "+tableName+"(id, cpu, mem, load, when, type) values (?,?,?,?,?,?)";
				
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = conn();
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setInt(parameterIndex++, cpu);
			pstmt.setInt(parameterIndex++, mem);
			pstmt.setDouble(parameterIndex++, load);
			pstmt.setTimestamp(parameterIndex++, when);
			pstmt.setString(parameterIndex++, type);
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
		String countSQL = "SELECT count(id) FROM "+tableName;
		int totalCount = 0;
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = conn();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
			if(rs.next()){
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
			return 0;
		} finally {
			releaseResource(stmt, rs);
			releaseConnection(conn);
		}
		return totalCount;
	}
	
	public List<SystemMonInfoHDWMY> select(int startRow, int length) {
		List<SystemMonInfoHDWMY> result = new ArrayList<SystemMonInfoHDWMY>();
		String countSQL = "SELECT max(id) FROM "+tableName;
		String selectSQL = "SELECT id, cpu, mem, load, when, type" +
				" FROM "+tableName+" WHERE id > ? and id <= ? order by id desc";
		
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
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
				SystemMonInfoHDWMY r = new SystemMonInfoHDWMY();
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
			releaseResource(stmt, pstmt, rs);
			releaseConnection(conn);
		}
		
		return result;
	}
	
	public List<SystemMonInfoHDWMY> select(Timestamp start, Timestamp end, String type) {
		String selectSQL = "SELECT id, cpu, mem, load, when, type" +
				" FROM "+tableName+" WHERE when >= ? and when <= ? and type = ? order by id desc";
		List<SystemMonInfoHDWMY> result = new ArrayList<SystemMonInfoHDWMY>();
		
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
				SystemMonInfoHDWMY r = new SystemMonInfoHDWMY();
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
	
	public int testAndCreate() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = conn();
			pstmt = conn.prepareStatement("select count(*) from "+tableName);
			rs = pstmt.executeQuery();
			rs.next();
			return 0;
		} catch (SQLException e) {
			create();
			return 1;
		}  finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
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

