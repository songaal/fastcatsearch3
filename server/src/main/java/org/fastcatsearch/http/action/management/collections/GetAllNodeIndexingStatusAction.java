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
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

//@ActionMapping(value = "/management/collections/all-node-indexing-status", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
@ActionMapping(value = "/management/collections/all-node-indexing-status", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.NONE)
public class GetAllNodeIndexingStatusAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionConfig collectionConfig = irService.collectionContext(collectionId).collectionConfig();

		String indexNodeId = collectionConfig.getIndexNode();
		List<String> dataNodeIdList = collectionConfig.getDataNodeList();
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);
		List<Node> dataNodeList = nodeService.getNodeById(dataNodeIdList);
		dataNodeList.remove(indexNode); //색인노드가 data노드에 있다면 빼준다.
		
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>();
		
		if(indexNode != null && indexNode.isActive()){
			GetIndexingDataInfoJob job = new GetIndexingDataInfoJob();
			job.setArgs(collectionId);
			ResultFuture resultFuture1 = nodeService.sendRequest(indexNode, job);
			resultFutureList.add(resultFuture1);
		}else{
			resultFutureList.add(null);
		}
		
		for (Node dataNode : dataNodeList) {
			GetIndexingDataInfoJob job = new GetIndexingDataInfoJob();
			job.setArgs(collectionId);
			if(dataNode != null && dataNode.isActive()){
				ResultFuture resultFuture = nodeService.sendRequest(dataNode, job);
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
		
		IndexingDataInfo indexingDataInfo = indexingDataInfoList.remove(0);
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		responseWriter.object();
		
		responseWriter.key("collectionId").value(collectionId);
		
		responseWriter.key("indexNode");
		writeIndexingDataInfo(indexNode, responseWriter, indexingDataInfo);
		
		responseWriter.key("dataNode").array();
		for (int i = 0; i < indexingDataInfoList.size(); i++) {
			IndexingDataInfo indexingDataInfo2 = indexingDataInfoList.get(i);
			Node dataNode = dataNodeList.get(i);
			writeIndexingDataInfo(dataNode, responseWriter, indexingDataInfo2);
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

}
