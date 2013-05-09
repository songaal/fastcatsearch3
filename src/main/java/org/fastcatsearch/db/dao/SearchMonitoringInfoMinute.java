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
import org.fastcatsearch.db.vo.SearchMonitoringInfoMinuteVO;

public class SearchMonitoringInfoMinute extends DAOBase {
	
	public SearchMonitoringInfoMinute(ConnectionManager connectionManager) {
		super(connectionManager);
	}
	@Override
	public boolean testTable() {
		return testQuery("select id, collection, hit, fail, achit, acfail, ave_time, max_time, when from " + tableName);
	}
	
	@Override
	public boolean createTable() throws SQLException {
		String createSQL = "create table "+tableName+"(id int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) primary key, collection varchar(20), hit int, fail int, achit int, acfail int, ave_time int, max_time int, when timestamp)";
		executeUpdate(createSQL);
		return true;
	}
	
	public int insert(String collection, int hit, int fail, int achit, int acfail, int ave_time, int max_time, Timestamp when) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String insertSQL = "insert into "+tableName+"(collection, hit, fail, achit, acfail, ave_time, max_time, when) values (?,?,?,?,?,?,?,?)";
		
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
	
	public List<SearchMonitoringInfoMinuteVO> select(int startRow, int length) {
		List<SearchMonitoringInfoMinuteVO> result = new ArrayList<SearchMonitoringInfoMinuteVO>();
		String selectSQL = "SELECT id, collection, hit, fail, achit, acfail, ave_time, max_time, when" +
				" FROM "+tableName+" WHERE id > ? and id <= ? order by id desc";
				
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
			int totalCount = selectCount();
			
			conn = conn();
		
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
//			logger.debug("Start = "+(totalCount - length)+ "~"+(totalCount - startRow));
			while(rs.next()){
				SearchMonitoringInfoMinuteVO r = new SearchMonitoringInfoMinuteVO();
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
	
	public List<SearchMonitoringInfoMinuteVO> select(Timestamp start, Timestamp end, String collection) {
		List<SearchMonitoringInfoMinuteVO> result = new ArrayList<SearchMonitoringInfoMinuteVO>();
		String selectSQL = "SELECT id, collection, hit, fail, achit, acfail, ave_time, max_time, when" +
				" FROM "+tableName+" WHERE when >= ? and when <= ? and collection = ? order by id desc";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try{
			conn = conn();
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setTimestamp(parameterIndex++, start);
			pstmt.setTimestamp(parameterIndex++, end);
			pstmt.setString(parameterIndex++, collection);
			rs = pstmt.executeQuery();
			logger.debug(collection+" = "+start+ "~"+end);
			while(rs.next()){
				SearchMonitoringInfoMinuteVO r = new SearchMonitoringInfoMinuteVO();
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

