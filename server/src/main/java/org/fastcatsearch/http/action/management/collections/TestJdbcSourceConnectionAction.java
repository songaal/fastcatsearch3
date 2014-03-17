package org.fastcatsearch.http.action.management.collections;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.DynamicClassLoader;
import org.fastcatsearch.util.ResponseWriter;

/**
 * jdbc 소스가 연결이 올바른지 확인.
 * 
 * */
@ActionMapping(value = "/management/collections/test-jdbc-source", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class TestJdbcSourceConnectionAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		String message = "";
		try {
			
			String driver = request.getParameter("driver");
			String url = request.getParameter("url");
			String user = request.getParameter("user");
			String password = request.getParameter("password");
			
			DynamicClassLoader.loadClass(driver);
			
			try {
				Connection connection = DriverManager.getConnection(url, user, password);
				isSuccess = connection != null;
			}catch(SQLException e){
				isSuccess = false;
//				StringWriter sw = new StringWriter();
//				PrintWriter w = new PrintWriter(sw);
//				e.printStackTrace(w);
//				message = sw.toString();
				message = e.getMessage();
			}
			
		} catch (Exception e) {
			logger.error("",e);
			isSuccess = false;
		}
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.key("message").value(message);
		responseWriter.endObject();
		responseWriter.done();
	}
}