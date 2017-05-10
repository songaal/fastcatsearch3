package org.fastcatsearch.http.action.management.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.management.UpdateCollectionConfigJob;
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
		String searchNodeList = request.getParameter("searchNodeList");
		String dataNodeList = request.getParameter("dataNodeList");
		int dataSequenceCycle = request.getIntParameter("dataSequenceCycle");
		int segmentRevisionBackupSize = request.getIntParameter("segmentRevisionBackupSize");
		int segmentDocumentLimit = request.getIntParameter("segmentDocumentLimit");
		int fullIndexingSegmentSize = request.getIntParameter("fullIndexingSegmentSize");
		int fullIndexingAlertTimeout = request.getIntParameter("fullIndexingAlertTimeout");
		int addIndexingAlertTimeout = request.getIntParameter("addIndexingAlertTimeout");
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		CollectionConfig collectionConfig = collectionContext.collectionConfig();
		collectionConfig.setName(collectionName);
		collectionConfig.setIndexNode(indexNode.trim());
		collectionConfig.getDataPlanConfig().setDataSequenceCycle(dataSequenceCycle);
		collectionConfig.getDataPlanConfig().setSegmentRevisionBackupSize(segmentRevisionBackupSize);
		collectionConfig.getDataPlanConfig().setSegmentDocumentLimit(segmentDocumentLimit);
		collectionConfig.setFullIndexingSegmentSize(fullIndexingSegmentSize);
		collectionConfig.setFullIndexingAlertTimeout(fullIndexingAlertTimeout);
		collectionConfig.setAddIndexingAlertTimeout(addIndexingAlertTimeout);
		
		List<String> searchNodeListObj = new ArrayList<String>();
		for(String nodeStr : searchNodeList.split(",")){
			nodeStr = nodeStr.trim();
			if(nodeStr.length() > 0){
				searchNodeListObj.add(nodeStr);
			}
		}
		collectionConfig.setSearchNodeList(searchNodeListObj);
		
		List<String> dataNodeListObj = new ArrayList<String>();
		for(String nodeStr : dataNodeList.split(",")){
			nodeStr = nodeStr.trim();
			if(nodeStr.length() > 0){
				dataNodeListObj.add(nodeStr);
			}
		}
		collectionConfig.setDataNodeList(dataNodeListObj);
		
		boolean isSuccess = CollectionContextUtil.writeConfigFile(collectionConfig, collectionContext.collectionFilePaths());
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		nodeService.updateLoadBalance(collectionId, dataNodeListObj);
		
		
		//master노드의 컬렉션셋팅 업데이트가 성공했다면 나머지 노드에 수행한다.
		if(isSuccess){
			logger.info("[{}] Master Update collection config success!", collectionId);
			Set<String> nodeSet = collectionConfig.getCollectionNodeIDSet();
			nodeSet.remove(environment.myNodeId());
			UpdateCollectionConfigJob job = new UpdateCollectionConfigJob(collectionId, collectionConfig);
			
			NodeJobResult[] resultList = ClusterUtils.sendJobToNodeIdSet(job, nodeService, nodeSet, false);
			for(NodeJobResult result : resultList){
				logger.debug("[{}] [{}] [{}] Node Update collection config.", result.isSuccess(), result.node(), collectionId);
			}
			
			
		}else{
			logger.error("[{}] Master Update collection config fail!", collectionId);
			
		}
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
		
	}

}
