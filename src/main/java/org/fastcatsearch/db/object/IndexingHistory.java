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

public class IndexingHistory extends DAOBase {

	public int id;
	public String collection;
	public String type;
	public boolean isSuccess;
	public int docSize;
	public int updateSize;
	public int deleteSize;
	public boolean isScheduled;
	public Timestamp startTime;
	public Timestamp endTime;
	public int duration;
	
	public IndexingHistory(){ }
	
	public int create() throws SQLException{
		String createSQL = "create table " + tableName + "(id int primary key, collection varchar(20), type char(1), isSuccess smallint, docSize int, updateSize int, deleteSize int, isScheduled smallint, startTime timestamp, endTime timestamp, duration int)";
		Statement stmt = conn.createStatement();
		return stmt.executeUpdate(createSQL);
	}
	
	public int insert(String collection, String type, boolean isSuccess, int docSize, int updateSize, int deleteSize, boolean isScheduled, Timestamp startTime, Timestamp endTime, int duration) {
		PreparedStatement pstmt = null;
		try{
			String insertSQL = "insert into " + tableName + "(id, collection, type, isSuccess, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration) values (?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, ID);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			pstmt.setBoolean(parameterIndex++, isSuccess);
			pstmt.setInt(parameterIndex++, docSize);
			pstmt.setInt(parameterIndex++, updateSize);
			pstmt.setInt(parameterIndex++, deleteSize);
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
	
	public List<IndexingHistory> select(int startRow, int length) {
		List<IndexingHistory> result = new ArrayList<IndexingHistory>();
		
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
			
			if(totalCount - startRow <= 0)
				return result;
			
			String selectSQL = "SELECT id, collection, type, isSuccess, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration" +
					" FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + tableName + ".* FROM " + tableName + " ) AS tmp WHERE rownum > ? and rownum <= ? order by id desc";
			PreparedStatement pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, totalCount - startRow - length);
			pstmt.setInt(parameterIndex++, totalCount - startRow);
			rs = pstmt.executeQuery();
//			logger.debug("totalCount = "+totalCount+", startRow="+startRow+", Start = "+(totalCount - startRow - length)+ "~"+(totalCount - startRow));
			while(rs.next()){
				IndexingHistory r = new IndexingHistory();
				
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.isSuccess = rs.getBoolean(parameterIndex++);
				r.docSize = rs.getInt(parameterIndex++);
				r.updateSize = rs.getInt(parameterIndex++);
				r.deleteSize = rs.getInt(parameterIndex++);
				r.isScheduled = rs.getBoolean(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.endTime = rs.getTimestamp(parameterIndex++);
				r.duration = rs.getInt(parameterIndex++);
				
				result.add(r);
			}
			
			pstmt.close();
			rs.close();
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
		}
		
		return result;
	}
	
	public int testAndCreate() throws SQLException {
		try {
			if ( isExists() == false )
				create();
			//의미없는 조건을 주어 실제결과를 가져오지 않도록 함.
			conn.prepareStatement("select id, collection, type, isSuccess, docSize, updateSize, deleteSize, isScheduled, startTime, endTime, duration from " + tableName + " where id = 0").executeQuery().next();
			return 0;
		} catch (SQLException e) {
			//table에 컬럼이 없을 경우에 exception이 발행하므로, table을 drop하고 재생성한다. 
			drop();
			create();
			return 1;
		}
	}

	private void drop() {
		PreparedStatement pstmt = null;
		try{
			String insertSQL = "drop table " + tableName;
			pstmt = conn.prepareStatement(insertSQL);
			pstmt.executeUpdate();
			logger.info(insertSQL);
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
		}finally{
			if(pstmt!=null) try { pstmt.close(); } catch (SQLException e) { } 
		}
		
	}
}
