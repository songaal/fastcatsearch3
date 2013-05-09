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

public class SearchMonitoringInfo extends DAOBase {
	
	public int id;
	public String collection;
	public int hit;
	public int fail;
	public int achit;
	public int acfail;
	public int ave_time;
	public int max_time;
	public Timestamp when;
	public String type;
	
	public SearchMonitoringInfo(){ }
	
	@Override
	public boolean testTable() {
		return testQuery("select id, collection, hit, fail, achit, acfail, ave_time, max_time, when, type from " + tableName);
	}
	
	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table "+tableName+"(id int GENERATED ALWAYS AS IDENTITY primary key, collection varchar(20), hit int, fail int, achit int, acfail int, ave_time int, max_time int, when timestamp, type varchar(1))";
		executeUpdate(createSQL);
		return true;
	}
	
	public int insert(String collection, int hit, int fail, int achit, int acfail, int ave_time, int max_time, Timestamp when, String type) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String insertSQL = "insert into "+tableName+"(collection, hit, fail, achit, acfail, ave_time, max_time, when, type) values (?,?,?,?,?,?,?,?,?)";
		
		try{
			conn = conn();
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setInt(parameterIndex++, hit);
			pstmt.setInt(parameterIndex++, fail);
			pstmt.setInt(parameterIndex++, achit);
			pstmt.setInt(parameterIndex++, acfail);
			pstmt.setInt(parameterIndex++, ave_time);
			pstmt.setInt(parameterIndex++, max_time);
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

	
	public List<SearchMonitoringInfo> select(int startRow, int length, String type) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String selectSQL = "SELECT id, collection, hit, fail, achit, acfail, ave_time, max_time, when, type" +
				" FROM "+tableName+" WHERE id > ? and id <= ? and type = ? order by id desc";
		
		List<SearchMonitoringInfo> result = new ArrayList<SearchMonitoringInfo>();
		
		try{
			int totalCount = selectCount();
			
			conn = conn();
			
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			pstmt.setString(parameterIndex++, type);
			rs = pstmt.executeQuery();
//			logger.debug("Start = "+(totalCount - length)+ "~"+(totalCount - startRow));
			while(rs.next()){
				SearchMonitoringInfo r = new SearchMonitoringInfo();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.collection = rs.getString(parameterIndex++);
				r.hit = rs.getInt(parameterIndex++);
				r.fail = rs.getInt(parameterIndex++);
				r.achit = rs.getInt(parameterIndex++);
				r.acfail = rs.getInt(parameterIndex++);
				r.ave_time = rs.getInt(parameterIndex++);
				r.max_time = rs.getInt(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				
				result.add(r);
			}
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(rs, pstmt);
			releaseConnection(conn);
		}
		
		return result;
	}
	
	public List<SearchMonitoringInfo> select(Timestamp start, Timestamp end, String collection, String type) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT id, collection, hit, fail, achit, acfail, ave_time, max_time, when, type" +
				" FROM "+tableName+" WHERE when >= ? and when <= ? and collection = ? and type = ? order by id desc";
				
		List<SearchMonitoringInfo> result = new ArrayList<SearchMonitoringInfo>();
		
		try{
			conn = conn();
			
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setTimestamp(parameterIndex++, start);
			pstmt.setTimestamp(parameterIndex++, end);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			rs = pstmt.executeQuery();
			logger.debug(collection+" = "+start+ "~"+end);
			while(rs.next()){
				SearchMonitoringInfo r = new SearchMonitoringInfo();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.collection = rs.getString(parameterIndex++);
				r.hit = rs.getInt(parameterIndex++);
				r.fail = rs.getInt(parameterIndex++);
				r.achit = rs.getInt(parameterIndex++);
				r.acfail = rs.getInt(parameterIndex++);
				r.ave_time = rs.getInt(parameterIndex++);
				r.max_time = rs.getInt(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				result.add(r);
			}
		} catch(SQLException e){
			logger.error(e.getMessage(),e);
		} finally {
			releaseResource(rs, pstmt);
			releaseConnection(conn);
		}
		return result;
	}
	
	public int deleteOld(int month) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String deleteSQL = "Delete From "+tableName+" Where when < ?";
				
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
