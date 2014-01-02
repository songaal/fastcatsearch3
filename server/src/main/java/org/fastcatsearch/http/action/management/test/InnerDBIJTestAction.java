package org.fastcatsearch.http.action.management.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;

import org.apache.derby.tools.ij;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceManager;
/*
 * using ij
 * */
@ActionMapping("/management/test/db/ij")
public class InnerDBIJTestAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String sql = request.getParameter("sql");
		String db = request.getParameter("db");
		
		Writer writer = response.getWriter();
		try {
			InternalDBModule internalDBModule = null;
			if(db.equalsIgnoreCase("system")){
				DBService dbService = ServiceManager.getInstance().getService(DBService.class);
				if (dbService == null) {
					writer.write("ERROR : DBService is not running.");
				}
				internalDBModule = dbService.internalDBModule();
			}else if(db.startsWith("plugin")){
				String[] els = db.split("/");
				String pluginId = els[1];
				PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
				if (pluginService == null) {
					writer.write("ERROR : PluginService is not running.");
					return;
				}
				Plugin plugin = pluginService.getPlugin(pluginId);
				if(plugin == null){
					writer.write("ERROR : Cannot find plugin > " + pluginId);
					return;
				}
				internalDBModule = plugin.internalDBModule();
			}
			
			
			SqlSession sqlSession = internalDBModule.openSession();
			try{
			
				Connection connection = sqlSession.getConnection();
				ByteArrayOutputStream resultOutput = new ByteArrayOutputStream();
				ij.runScript(connection, new ByteArrayInputStream(sql.getBytes()), "UTF-8", resultOutput, "UTF-8");
				
				String resultString = resultOutput.toString();
				writer.write(resultString);
				
			} finally {
	
				if (sqlSession != null) {
					sqlSession.close();
				}
			}
		}catch(Exception e){
			e.printStackTrace(new PrintWriter(writer));
		} finally {

			if(writer != null){
				writer.close();
			}
		}
	}
}
