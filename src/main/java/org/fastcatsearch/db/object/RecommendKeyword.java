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

import org.fastcatsearch.db.object.MapDictionaryDAO;

public class RecommendKeyword extends MapDictionaryDAO {

	public int id;
	public String dickey;
	public int count;
	public String value;

	public RecommendKeyword() {
	}

	public RecommendKeyword exactSearch(String keyword) {
		String selectSQL = "SELECT * FROM " + tableName + " where dickey=?";
		
		RecommendKeyword r = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		
		try {
			conn = conn();
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
			logger.error(e.getMessage(), e);
		} finally {
			releaseResource(pstmt, rs);
			releaseConnection(conn);
		}
		return r;
	}
}
