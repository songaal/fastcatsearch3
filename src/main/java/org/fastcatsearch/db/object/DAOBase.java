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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;

import org.fastcatsearch.db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DAOBase {
		
		protected static final Logger logger = LoggerFactory.getLogger(DAOBase.class);
		
		protected ConnectionManager connectionManager;
		protected int ID = -1;
		
		public DAOBase(){ }
		
		public boolean isExists()
		{
			if ( connectionManager == null )
				return false;
			
			Connection conn = null;
			try {
				conn = connectionManager.getConnection();
				DatabaseMetaData dbmeta = conn.getMetaData();
				ResultSet rs = dbmeta.getTables(null, null, "%", null);
				TreeSet<String> ts = new TreeSet<String>();
				while ( rs.next() )
				{
					ts.add(rs.getString(3).toLowerCase());
				}
				rs.close();
				return ts.contains(tableName.trim().toLowerCase());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(conn != null){
					try {
						conn.close();
					} catch (SQLException e) {
					}
				}
			}
			return false;
		}
		
		public void setConnectionManager(ConnectionManager connectionManager) {
			this.connectionManager = connectionManager;			
		}
		
		public Connection conn() throws SQLException{
			return connectionManager.getConnection();
		}
		
		public void releaseResource(Object... objList) {
			if(objList == null){
				return;
			}
			
			for (int i = 0; i < objList.length; i++) {
				Object obj = objList[i];
				if(obj == null){
					continue;
				}
				
				if(obj instanceof Statement){
					try {
						((Statement) obj).close();
					} catch (SQLException ignore) {
					}
				}else if(obj instanceof PreparedStatement){
					try {
						((PreparedStatement) obj).close();
					} catch (SQLException ignore) {
					}
				}else if(obj instanceof ResultSet){
					try {
						((ResultSet) obj).close();
					} catch (SQLException ignore) {
					}
				}
			}
		}
		
		//릴리즈된 연결처리는 connectionManager에 위임한다. 
		protected void releaseConnection(Connection conn) {
			connectionManager.releaseConnection(conn);
		}
		
		public abstract int testAndCreate() throws SQLException;
//		public abstract int createBody(Statement stmt) throws SQLException;
//		
//		public int create() throws SQLException {
//			Connection conn = null;
//			Statement stmt = null;
//			
//			try
//			{
//				conn = conn();
//				stmt = conn.createStatement();
//				return createBody(stmt);				
//			} finally {
//				releaseConnection(conn);
//				releaseResource(stmt);
//			}
//			
//		}
			
		public void prepareID(){
			if(ID != -1) return;
			
			String selectIdSQL = "select case when max(id) is null then 0 else max(id) + 1 end as id from "+tableName;
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			try {
				conn = connectionManager.getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery(selectIdSQL);
				if(rs.next()){
					ID = rs.getInt(1);
				}
				logger.debug("{} ID = {}", tableName, ID);
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);
			} finally {
				if(rs != null){
					try {
						rs.close();
					} catch (SQLException e) {
					}
				}
				if(stmt != null){
					try {
						stmt.close();
					} catch (SQLException e) {
					}
				}
				if(conn != null){
					try {
						conn.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		protected String tableName = this.getClass().getSimpleName();
}
