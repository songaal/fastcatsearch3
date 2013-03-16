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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicDictionary extends SetDictionaryDAO  {

	public BasicDictionary() 
	{ 
		this.tableName = "basicdictionary";
		this.fieldName = "basicword";		
	}	
	
	public boolean bulkInsert(File file){
		
		PreparedStatement pstmt = null;
		try {
			String cleanSQL = "truncate table " + tableName;
			logger.debug(cleanSQL);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(cleanSQL);
			stmt.close();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) { }
				
			String insertSQL = "insert into " + tableName + "(id, basicword) values (?,?)";
			logger.debug(insertSQL);
			pstmt = conn.prepareStatement(insertSQL);
			conn.setAutoCommit(false);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
			String basicword = null;
			int i = 1;
			while((basicword = br.readLine()) != null){
				if(basicword.startsWith("//") || basicword.length() == 0)
					continue;
				
				int p = basicword.indexOf("/");
				if(p > 0){
					basicword = basicword.substring(0, p);
				}
				pstmt.setInt(1, i);
				pstmt.setString(2, basicword);
				i++;
				try{
					pstmt.executeUpdate();
				} catch (SQLException e) {
					logger.debug("b = "+basicword);
					logger.error(e.getMessage(),e);
				}
				//중복단어가 존재함..
//				pstmt.addBatch();
//				if(i % 10 == 0){
//					pstmt.executeBatch();
//					conn.commit();
//				}
			}
			//여기서 에러가 발생하여 commit이 안되는 버그수정.
			//2012-01-12 swsong
//			pstmt.executeUpdate();
			br.close();
			conn.commit();
			return true;
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			return false;
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			return false;
		} finally {
			try {
				if(pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
			}
		}
		
		
	}	
}
