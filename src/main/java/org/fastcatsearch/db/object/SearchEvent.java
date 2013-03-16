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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SearchEvent extends DAOBase {
	
	public int id;
	public Timestamp when;
	public String type;
	public int category;
	public String summary;
	public String stacktrace;
	public String status;
	
	public SearchEvent(){ }
	
	public int create() throws SQLException{
		String createSQL = "create table " + tableName + "(id int primary key, when timestamp, type char(5), category int, summary varchar(200), stacktrace varchar(3000), status char(1))";
		Statement stmt = conn.createStatement();
		return stmt.executeUpdate(createSQL);
	}
	
	public int insert(Timestamp when, String type, int category, String summary, String stacktrace, String status) {
		
		PreparedStatement pstmt = null;
		try{
			String insertSQL = "insert into " + tableName + "(id, when, type, category, summary, stacktrace, status) values (?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setTimestamp(parameterIndex++, when);
			pstmt.setString(parameterIndex++, type);
			pstmt.setInt(parameterIndex++, category);
			pstmt.setString(parameterIndex++, summary);
			pstmt.setString(parameterIndex++, stacktrace);
			pstmt.setString(parameterIndex++, status);
			int c =  pstmt.executeUpdate();
			if(c > 0){
				ID++;
			}
			return c;
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			return -1;
		}finally{
			if(pstmt!=null) try { pstmt.close(); } catch (SQLException e) { }
		}
	}

	public int count() {
		try{
			String countSQL = "SELECT count(id) FROM " + tableName;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if(rs.next()){
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		
			return totalCount;
			
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			return 0;
		}
	}
	
	public List<SearchEvent> select(int startRow, int length) {
		List<SearchEvent> result = new ArrayList<SearchEvent>();
		try{
			
			String countSQL = "SELECT count(id) FROM " + tableName;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(countSQL);
			int totalCount = 0;
			if(rs.next()){
				totalCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		
			String selectSQL = "SELECT id, when, type, category, summary, stacktrace, status" +
					" FROM SearchEvent WHERE id > ? and id <= ? order by id desc";
			PreparedStatement pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
//			logger.debug("Start = {} ~ {}", (totalCount - length), (totalCount - startRow));
			while(rs.next()){
				SearchEvent r = new SearchEvent();
				
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.when = rs.getTimestamp(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.category = rs.getInt(parameterIndex++);
				r.summary = rs.getString(parameterIndex++);
				r.stacktrace = rs.getString(parameterIndex++);
				r.status = rs.getString(parameterIndex++);
				
				result.add(r);
			}
			
			pstmt.close();
			rs.close();
			
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
		}
		
		return result;
	}
	
	//최근업데이트건의 최근 시간만 가져온다.
	public Timestamp isUpdated(Timestamp lastTime) {
		try{
			String checkSQL = "select when from SearchEvent " +
					"where when > ? " +
					"order by when desc";
			PreparedStatement pstmt = conn.prepareStatement(checkSQL);
			pstmt.setTimestamp(1, lastTime);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				return rs.getTimestamp(1);
			}
			
			return null;
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			return null;
		}
	}
	
	public int testAndCreate() throws SQLException {
		try {
			conn.prepareStatement("select count(*) from SearchEvent").executeQuery().next();
			return 0;
		} catch (SQLException e) {
			create();
			return 1;
		}
	}
	
	public int update(int id, String status) {
		logger.debug("id={}, status={}", id, status);
		try{
			String updateSQL = "update SearchEvent set status=?" +
					"where id=?";
			PreparedStatement pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, status.toUpperCase());
			pstmt.setInt(parameterIndex++, id);
			return pstmt.executeUpdate();
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			return -1;
		}
	}
}
