package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.JDBCSourceConfig;
import org.fastcatsearch.ir.config.JDBCSourceInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/update-jdbc-source", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateCollectionJdbcSourceAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		
		try {
			
			String collectionId = request.getParameter("collectionId");
			int sourceIndex = request.getIntParameter("sourceIndex",-1);
			
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String driver = request.getParameter("driver");
			String url = request.getParameter("url");
			String user = request.getParameter("user");
			String password = request.getParameter("password");
			String mode = request.getParameter("mode");
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			List<JDBCSourceInfo> sourceList = irService.getJDBCSourceConfig().getJdbcSourceInfoList();
			JDBCSourceInfo source = null;
			
			
			File jdbcSourceConfigFile = (new File("",SettingFileNames.jdbcSourceConfig));
			
			if(sourceIndex==-1) {
				if(sourceList==null) {
					sourceList = new ArrayList<JDBCSourceInfo>();
					irService.getJDBCSourceConfig().setJdbcSourceInfoList(sourceList);
				}
				source = new JDBCSourceInfo();
				sourceList.add(source);
			} else {
				if(sourceList!=null && sourceList.size() > sourceIndex) {
					source = sourceList.get(sourceIndex);
					if("".equals(password)) {
						password = source.getPassword();
					}
				}
			}
			
			if("delete".equals(mode)) {
				sourceList.remove(sourceIndex);
			} else {
				if(source!=null) {
					source.setId(id);
					source.setName(name);
					source.setDriver(driver);
					source.setUrl(url);
					source.setUser(user);
					source.setPassword(password);
				}
			}
			
			irService.updateJDBCSourceConfig(irService.getJDBCSourceConfig());
		
			isSuccess = true;
			
		} catch (Exception e) {
			logger.error("",e);
			isSuccess = false;
		}
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
	}
}