package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
	
//	public void test() throws Exception {
//		
//		final NetworkServerControlImpl server = new NetworkServerControlImpl(InetAddress.getByName("localhost"),9999,"","");
//		server.start(null);
//		
//		Class.forName("org.apache.derby.jdbc.ClientDriver");
//		Connection conn = DriverManager.getConnection("jdbc:derby://localhost:9999//home/lupfeliz/fastcatsearch-1.9/db");
//		
//		PreparedStatement pst = conn.prepareStatement("SELECT * FROM JOBHISTORY");
//		
//		ResultSet res = pst.executeQuery();
//		
//		while(res.next()) {
//			
//			System.out.println(res.getString("JOBCLASSNAME")+":"+res.getString("ARGS"));
//			
//		}
//	}
}
