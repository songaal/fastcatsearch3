/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.TestCase;

public class BlobTest extends TestCase {
	public void testInsert(){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/test" ,"root" ,"1234");
			conn.setAutoCommit(true);
			PreparedStatement pstmt = conn.prepareStatement("insert into gallery1(image,title,filesize) values (?,?,?);");
			int parameterIndex = 1;
			File f = new File("c:\\rc4.log");
			FileInputStream fis = new FileInputStream(f);
			pstmt.setBinaryStream(parameterIndex++, fis, (int) f.length());
			pstmt.setString(parameterIndex++, "title-1");
			pstmt.setInt(parameterIndex++, 12314);
			pstmt.executeUpdate();
			fis.close();
			
		} catch (Exception  e) {
			e.printStackTrace();
		} finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
