package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.job.management.GetIndexingDataInfoJob;
import org.fastcatsearch.job.management.GetIndexingDataInfoJob.IndexingDataInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

@ActionMapping(value = "/management/collections/all-collection-indexing-status", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.NONE)
public class GetAllCollectionIndexingStatusAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String nodeId = request.getParameter("nodeId");

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		List<Collection> collectionList = irService.getCollectionList();
		
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>();
		
		for (int inx = 0; inx < collectionList.size(); inx++) {
			String collectionId = collectionList.get(inx).getId();

			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(nodeId);
			GetIndexingDataInfoJob job = new GetIndexingDataInfoJob();
			job.setArgs(collectionId);
			ResultFuture resultFuture1 = nodeService.sendRequest(indexNode, job);
			resultFutureList.add(resultFuture1);
			
		}
		
		List<IndexingDataInfo> indexingDataInfoList = new ArrayList<IndexingDataInfo>();
		
		for (ResultFuture resultFuture : resultFutureList) {
			if (resultFuture != null) {
				Object obj = resultFuture.take();
				if (obj != null && obj instanceof IndexingDataInfo) {
					indexingDataInfoList.add((IndexingDataInfo) obj);
				} else {
					indexingDataInfoList.add(null);
				}
			} else {
				indexingDataInfoList.add(null);
			}
		}
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		responseWriter.object();
		
		responseWriter.key("nodeId").value(nodeId);
		
		responseWriter.key("indexingState").array();
		for (int i = 0; i < indexingDataInfoList.size(); i++) {
			
			String collectionId = collectionList.get(i).getId();
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if(collectionContext != null) {
				CollectionConfig collectionConfig = collectionContext.collectionConfig();
				String collectionName = collectionConfig.getName();
				IndexingDataInfo indexingDataInfo2 = indexingDataInfoList.get(i);
				writeIndexingDataInfo(collectionId, collectionName, responseWriter, indexingDataInfo2);
			}
		}
		responseWriter.endArray();
		
		responseWriter.endObject();
		
		responseWriter.done();
	}

	private void writeIndexingDataInfo(String collectionId, String collectionName, ResponseWriter responseWriter, IndexingDataInfo indexingDataInfo) throws ResultWriterException {
		
		responseWriter.object();
		responseWriter
		.key("collectionId").value(collectionId)
		.key("collectionName").value(collectionName);
		
		if(indexingDataInfo != null){
			responseWriter
			.key("segmentSize").value(indexingDataInfo.segmentSize)
			.key("sequence").value(indexingDataInfo.sequence)
			.key("dataPath").value(indexingDataInfo.dataPath)
			.key("diskSize").value(indexingDataInfo.diskSize)
			.key("documentSize").value(indexingDataInfo.documentSize)
            .key("deleteSize").value(indexingDataInfo.deleteSize)
			.key("createTime").value(indexingDataInfo.createTime);
		}
		
		responseWriter.endObject();
	}

}
