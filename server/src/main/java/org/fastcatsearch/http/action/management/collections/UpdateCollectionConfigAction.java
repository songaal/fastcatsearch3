package org.fastcatsearch.http.action.management.collections;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/update-config", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateCollectionConfigAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		String collectionName = request.getParameter("collectionName");
		String indexNode = request.getParameter("indexNode");
		String dataNodeList = request.getParameter("dataNodeList");
		int dataSequenceCycle = request.getIntParameter("dataSequenceCycle");
		int segmentRevisionBackupSize = request.getIntParameter("segmentRevisionBackupSize");
		int segmentDocumentLimit = request.getIntParameter("segmentDocumentLimit");
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		CollectionConfig collectionConfig = collectionContext.collectionConfig();
		collectionConfig.setName(collectionName);
		collectionConfig.setIndexNode(indexNode.trim());
		collectionConfig.getDataPlanConfig().setDataSequenceCycle(dataSequenceCycle);
		collectionConfig.getDataPlanConfig().setSegmentRevisionBackupSize(segmentRevisionBackupSize);
		collectionConfig.getDataPlanConfig().setSegmentDocumentLimit(segmentDocumentLimit);
		
		List<String> dataNodeListObj = new ArrayList<String>();
		for(String nodeStr : dataNodeList.split(",")){
			nodeStr = nodeStr.trim();
			if(nodeStr.length() > 0){
				dataNodeListObj.add(nodeStr);
			}
		}
		collectionConfig.setDataNodeList(dataNodeListObj);
		
		boolean isSuccess = CollectionContextUtil.updateConfig(collectionConfig, collectionContext.collectionFilePaths());
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
		
	}

}
