package org.fastcatsearch.db;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.IRSettings;
import org.junit.Test;

public class DerbyTest {

	@Test
	public void testConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		String JDBC_URL = "jdbc:derby:/tmp/a";
		Connection conn = null;
		Statement stmt = null;
		int COUNT = 100000;
		
		DriverManager.getConnection(JDBC_URL + ";create=true");
		
		long st = System.currentTimeMillis();
		long lap = st;
		for (int i = 0; i < COUNT; i++) {
			try{
				conn = DriverManager.getConnection(JDBC_URL);
				if(conn == null){
					System.out.println("create");
				}
				
				if( i % 1000 == 0){
					long lapTime = System.currentTimeMillis() - lap;
					System.out.println(i + "th time=" + lapTime + " "+ ((double)(lapTime) / (double)1000) +"ms/1conn");
					lap = System.currentTimeMillis();
				}
//				stmt = conn.createStatement();
			}finally{
				if(stmt != null){
					stmt.close();
				}
				
				if(conn != null){
					conn.close();
				}
			}
		}
		
		long elapsed = System.currentTimeMillis() - st;
		
		System.out.println("time = "+elapsed+" ms / "+COUNT+"count");
		
	}

}
