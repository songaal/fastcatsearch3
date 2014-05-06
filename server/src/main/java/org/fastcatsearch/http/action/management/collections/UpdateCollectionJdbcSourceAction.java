package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.JDBCSourceConfig;
import org.fastcatsearch.ir.config.JDBCSourceInfo;
import org.fastcatsearch.job.management.SyncJDBCSettingFileObjectJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/update-jdbc-source", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateCollectionJdbcSourceAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		
		try {
			
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
			
			/*
			 *  index 서버에 설정파일을 전송한다.
			 */
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Object settingObj = irService.getJDBCSourceConfig();
			
			//
			// 어느 색인노드가 jdbc를 사용할지 모르므로, 다 보낸다.
			//
			Set<String> indexNodeSet = new HashSet<String>();
			for(Collection collection : irService.getCollectionList()) {
				String collectionId = collection.getId();
				indexNodeSet.addAll(irService.collectionContext(collectionId).collectionConfig().getCollectionNodeIDSet());
			}
			
			for(String nodeId : indexNodeSet) {
				SyncJDBCSettingFileObjectJob job = new SyncJDBCSettingFileObjectJob(settingObj);
				Node node = nodeService.getNodeById(nodeId);
				ResultFuture resultFuture = nodeService.sendRequest(node, job);
				if(resultFuture != null) {
					resultFuture.take();
				}
			}
			
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