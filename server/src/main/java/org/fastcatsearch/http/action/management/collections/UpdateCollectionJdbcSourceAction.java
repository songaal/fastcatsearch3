package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.JDBCSourceInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/update-jdbc-source")
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
			
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			List<JDBCSourceInfo> sourceList = dataSourceConfig.getJdbcSourceInfoList();
			JDBCSourceInfo source = null;
			
			File dataSourceConfigFile = collectionContext.collectionFilePaths().file(
					SettingFileNames.datasourceConfig);
			
			if(sourceIndex==-1) {
				if(sourceList==null) {
					sourceList = new ArrayList<JDBCSourceInfo>();
					dataSourceConfig.setJdbcSourceInfoList(sourceList);
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
		
			JAXBConfigs.writeConfig(dataSourceConfigFile, dataSourceConfig, DataSourceConfig.class);
			
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