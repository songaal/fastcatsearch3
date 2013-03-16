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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DAOBase {
		
		protected static final Logger logger = LoggerFactory.getLogger(DAOBase.class);
		
		protected Connection conn;
		protected int ID = -1;
		public boolean isExists()
		{
			if ( conn == null )
				return false;
			
			try {
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
			}
			return false;
		}
		
		public void setConnection(Connection conn) {
			this.conn = conn;			
		}
		
		public Connection getConnection() { return conn; }
		
		public void prepareID(){
			if(ID != -1) return;
			
			String selectIdSQL = "select case when max(id) is null then 0 else max(id) + 1 end as id from "+tableName;
			try {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(selectIdSQL);
				if(rs.next()){
					ID = rs.getInt(1);
				}
				logger.debug("{} ID = {}", tableName, ID);
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);
			}
		}
		protected String tableName = this.getClass().getSimpleName();
}
