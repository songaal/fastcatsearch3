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

import org.fastcatsearch.db.object.MAPDictionaryDAO;

public class RecommendKeyword extends MAPDictionaryDAO {

	public int id;
	public String dickey;
	public int count;
	public String value;

	public RecommendKeyword() 
	{
	this.tableName = "RecommendKeyword"; 	
	}

	public RecommendKeyword exactSearch(String keyword) {
		RecommendKeyword r = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			String selectSQL = "SELECT * FROM " + tableName + " where dickey=?";
			pstmt = conn.prepareStatement(selectSQL);
			int parameterIndex = 1;
			pstmt.setString(parameterIndex++, keyword);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				r = new RecommendKeyword();
				parameterIndex = 1;
				r.id = rs.getInt(parameterIndex++);
				r.dickey = rs.getString(parameterIndex++);
				r.count = rs.getInt(parameterIndex++);
				r.value = rs.getString(parameterIndex++);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return r;
	}	

}
