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
import org.fastcatsearch.job.management.GetIndexingDataInfoJob;
import org.fastcatsearch.job.management.GetIndexingDataInfoJob.IndexingDataInfo;
import org.fastcatsearch.job.management.GetRestorableIndexingDataInfoJob;
import org.fastcatsearch.job.management.GetRestorableIndexingDataInfoJob.IndexingDataShortInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

@ActionMapping(value = "/management/collections/all-node-indexing-management-status", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetAllNodeIndexingManagementStatusAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionConfig collectionConfig = irService.collectionContext(collectionId).collectionConfig();

		String indexNodeId = collectionConfig.getIndexNode();
		List<String> dataNodeIdList = collectionConfig.getDataNodeList();
		
		List<String> nodeIdList = new ArrayList<String>(dataNodeIdList);
		//indexNodeId 까지 모두 합친다.
		if(!nodeIdList.contains(indexNodeId)){
			nodeIdList.add(indexNodeId);
		}
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		List<Node> nodeList = nodeService.getNodeById(nodeIdList);
		
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>();
		
		for (Node node : nodeList) {
			GetIndexingDataInfoJob job = new GetIndexingDataInfoJob();
			job.setArgs(collectionId);
			if(node != null && node.isActive()){
				ResultFuture resultFuture = nodeService.sendRequest(node, job);
				resultFutureList.add(resultFuture);
			}else{
				resultFutureList.add(null);
			}
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
		
		List<ResultFuture> resultFutureList2 = new ArrayList<ResultFuture>();
		
		for (Node node : nodeList) {
			GetRestorableIndexingDataInfoJob job = new GetRestorableIndexingDataInfoJob();
			job.setArgs(collectionId);
			if(node != null && node.isActive()){
				ResultFuture resultFuture = nodeService.sendRequest(node, job);
				resultFutureList2.add(resultFuture);
			}else{
				resultFutureList2.add(null);
			}
		}
		
		List<IndexingDataShortInfo> indexingDataShortInfoList = new ArrayList<IndexingDataShortInfo>();
		
		for (ResultFuture resultFuture : resultFutureList2) {
			if (resultFuture != null) {
				Object obj = resultFuture.take();
				if (obj != null && obj instanceof IndexingDataShortInfo) {
					indexingDataShortInfoList.add((IndexingDataShortInfo) obj);
				} else {
					indexingDataShortInfoList.add(null);
				}
			} else {
				indexingDataShortInfoList.add(null);
			}
		}
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		responseWriter.object();
		
		responseWriter.key("collectionId").value(collectionId);
		
		responseWriter.key("indexNodeId").value(indexNodeId);
		
		responseWriter.key("indexData").array();
		for (int i = 0; i < indexingDataInfoList.size(); i++) {
			IndexingDataInfo indexingDataInfo2 = indexingDataInfoList.get(i);
			Node dataNode = nodeList.get(i);
			writeIndexingDataInfo(dataNode, responseWriter, indexingDataInfo2);
		}
		responseWriter.endArray();
		
		responseWriter.key("restorableIndexData").array();
		for (int i = 0; i < indexingDataShortInfoList.size(); i++) {
			IndexingDataShortInfo indexingDataInfo2 = indexingDataShortInfoList.get(i);
			Node dataNode = nodeList.get(i);
			writeIndexingDataShortInfo(dataNode, responseWriter, indexingDataInfo2);
		}
		responseWriter.endArray();
		
		responseWriter.endObject();
		
		responseWriter.done();
	}

	private void writeIndexingDataInfo(Node node, ResponseWriter responseWriter, IndexingDataInfo indexingDataInfo) throws ResultWriterException {
		
		responseWriter.object();
		responseWriter
		.key("nodeId").value(node.id())
		.key("nodeName").value(node.name());
		
		if(indexingDataInfo != null){
			responseWriter
			.key("segmentSize").value(indexingDataInfo.segmentSize)
			.key("sequence").value(indexingDataInfo.sequence)
			.key("dataPath").value(indexingDataInfo.dataPath)
			.key("diskSize").value(indexingDataInfo.diskSize)
			.key("documentSize").value(indexingDataInfo.documentSize)
            .key("deleteSize").value(indexingDataInfo.deleteSize)
			.key("createTime").value(indexingDataInfo.createTime);
		}else{
			responseWriter
			.key("segmentSize").value(0)
			.key("sequence").value(-1)
			.key("dataPath").value("")
			.key("diskSize").value("")
			.key("documentSize").value(0)
            .key("deleteSize").value(0)
			.key("createTime").value("");
		}
		
		responseWriter.endObject();
	}
	
	private void writeIndexingDataShortInfo(Node node, ResponseWriter responseWriter, IndexingDataShortInfo indexingDataInfo) throws ResultWriterException {
		
		responseWriter.object();
		responseWriter
		.key("nodeId").value(node.id())
		.key("nodeName").value(node.name());
		
		if(indexingDataInfo != null){
			responseWriter
			.key("sequence").value(indexingDataInfo.sequence)
			.key("dataPath").value(indexingDataInfo.dataPath)
			.key("diskSize").value(indexingDataInfo.diskSize)
			.key("documentSize").value(indexingDataInfo.documentSize)
            .key("deleteSize").value(indexingDataInfo.deleteSize);
		}else{
            responseWriter
			.key("sequence").value(-1)
			.key("dataPath").value("")
			.key("diskSize").value("")
                    .key("documentSize").value(0)
            .key("deleteSize").value(0);
		}
		
		responseWriter.endObject();
	}

}
