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

public class IndexingSchedule extends DAOBase {

	public String collection;
	public String type;
	public int period;
	public Timestamp startTime;
	public boolean isActive;
	
	public IndexingSchedule(){ }
	
	public int create() throws SQLException{
		String createSQL = "create table " + tableName + "(collection varchar(20), type char(1), period int, startTime timestamp, isActive smallint)";
		Statement stmt = conn.createStatement();
		return stmt.executeUpdate(createSQL);
	}
	
	public int delete(String collection)
	{
		int result = 0;
		PreparedStatement pstmt = null;
		try
		{
		String deleteSQL = "delete from "  + tableName + " where collection = ?";
		pstmt = conn.prepareStatement(deleteSQL);
		int parameterIndex = 1;
		pstmt.setString(parameterIndex++, collection);
		result = 1;
		pstmt.executeUpdate();
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(), e);
			}			
		}
		return result;
	}
	
	public int updateOrInsert(String collection, String type, int period, Timestamp startTime, boolean isActive) {
		PreparedStatement pstmt = null;
		int result = 0;
		try{
			String checkSQL = "select count(collection) from " + tableName + " " +
					"where collection=? and type=?";
			pstmt = conn.prepareStatement(checkSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			if(rs.next()){
				count = rs.getInt(1);
			}
			
			if(count > 0){
				result =  update(collection, type, period, startTime, isActive);
			}else{
				result =  insert(collection, type, period, startTime, isActive);
			}
			
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			result =  -1;
		}finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(), e);
			}			
		}
		return result;
	}
	
	public int updateStatus(String collection, String type, boolean isActive) {
		PreparedStatement pstmt = null;
		int result = -1;
		try{
			String updateSQL = "update " + tableName + " set isActive=? " +
			"where collection=? and type=?";
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setBoolean(parameterIndex++, isActive);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			result =  pstmt.executeUpdate();
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			result =  -1;
		}finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(), e);
			}			
		}
		return result;
	}

	public int insert(String collection, String type, int period, Timestamp startTime, boolean isActive) {
		PreparedStatement pstmt = null;
		int result = -1;
		try{
			String insertSQL = "insert into " + tableName + "(collection, type, period, startTime, isActive) values (?,?,?,?,?)";
			pstmt = conn.prepareStatement(insertSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			pstmt.setInt(parameterIndex++, period);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setBoolean(parameterIndex++, isActive);
			result =  pstmt.executeUpdate();
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			result = -1;
		}finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}
	
	public int update(String collection, String type, int period, Timestamp startTime, boolean isActive) {
		PreparedStatement pstmt = null;
		int result = -1;
		try{
			String updateSQL = "update " + tableName + " set period=?, startTime=?, isActive=? " +
					"where collection=? and type=?";
			pstmt = conn.prepareStatement(updateSQL);
			int parameterIndex = 1;
			pstmt.setInt(parameterIndex++, period);
			pstmt.setTimestamp(parameterIndex++, startTime);
			pstmt.setBoolean(parameterIndex++, isActive);
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			result =  pstmt.executeUpdate();
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
			result = -1;
		}finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}
	
	public IndexingSchedule select(String collection, String type) {
		IndexingSchedule r = null;
		PreparedStatement pstmt = null;
		try{
			String selectSQL = "select collection, type, period, startTime, isActive from " + tableName + " " +
					"where collection=? and type=?";
			pstmt = conn.prepareStatement(selectSQL);
			
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, collection);
			pstmt.setString(parameterIndex++, type);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()){
				r = new IndexingSchedule();
				
				parameterIndex = 1;
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.period = rs.getInt(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.isActive = rs.getBoolean(parameterIndex++);
			}
		
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
		}finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(),e);
			}
		}
		return r;
	}
	
	public List<IndexingSchedule> selectAll() {
		List<IndexingSchedule> result = new ArrayList<IndexingSchedule>();
		PreparedStatement pstmt = null;
		try{
			String selectSQL = "select collection, type, period, startTime, isActive from " + tableName + " where isActive = 1";
			pstmt = conn.prepareStatement(selectSQL);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()){
				IndexingSchedule r = new IndexingSchedule();
				
				int parameterIndex = 1;
				r.collection = rs.getString(parameterIndex++);
				r.type = rs.getString(parameterIndex++);
				r.period = rs.getInt(parameterIndex++);
				r.startTime = rs.getTimestamp(parameterIndex++);
				r.isActive = rs.getBoolean(parameterIndex++);
				
				result.add(r);
			}
		}catch(SQLException e){
			logger.error(e.getMessage(),e);
		}finally
		{
			try
			{
			pstmt.close();
			}
			catch ( Exception e)
			{
				logger.error(e.getMessage(),e);
			}	
		}
		return result;
	}
	
	public int testAndCreate() throws SQLException {
		if ( isExists() == false )
			create();
		
		try {
			conn.prepareStatement("select count(*) from " + tableName).executeQuery().next();
			return 0;
		} catch (SQLException e) {
			create();
			return 1;
		}
	}
}
