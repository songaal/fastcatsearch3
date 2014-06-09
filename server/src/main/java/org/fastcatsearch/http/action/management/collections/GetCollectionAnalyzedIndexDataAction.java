package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
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
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.management.GetCollectionAnalyzedIndexDataJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.vo.CollectionAnalyzedIndexData;
import org.fastcatsearch.vo.CollectionIndexData;
import org.fastcatsearch.vo.CollectionIndexData.RowData;

@ActionMapping(value="/management/collections/index-data-analyzed", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetCollectionAnalyzedIndexDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		int start = Integer.parseInt(request.getParameter("start", "0"));
		int end = Integer.parseInt(request.getParameter("end", "0"));
		String pkValue = request.getParameter("pkValue");
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();

		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);
		
		
		GetCollectionAnalyzedIndexDataJob job = new GetCollectionAnalyzedIndexDataJob(collectionId, start, end, pkValue);
		ResultFuture resultFuture = nodeService.sendRequest(indexNode, job);

		CollectionAnalyzedIndexData data = null;

		if (resultFuture != null) {
			Object obj = resultFuture.take();
			if (obj instanceof CollectionIndexData) {
				data = (CollectionAnalyzedIndexData) obj;
			}
		}
		
		resultWriter.object()
		.key("collectionId").value(collectionId);
		if (data == null || data.getAnalyzedData() == null) {
			resultWriter.key("documentSize").value(0)
			.key("fieldList").array().endArray()
			.key("indexData").array().endArray();
		} else {
			resultWriter.key("documentSize").value(data.getDocumentSize());
			
			resultWriter.key("fieldList").array();
			for(String fieldId : data.getFieldList()) {
				resultWriter.value(fieldId);
			}
			resultWriter.endArray();
			
			resultWriter.key("indexData").array();
			
			List<RowData> indexDataList = data.getIndexData();
			List<RowData> pkDataList = data.getPkData();
			List<RowData> analyzedDataList = data.getAnalyzedData();
			List<Boolean> isDeletedList = data.getIsDeletedList();
			
			for(int i = 0; i < indexDataList.size(); i++) {
				RowData pkData = null;
				RowData indexData = indexDataList.get(i);
				RowData analyzedData = analyzedDataList.get(i);
				
				if(pkDataList != null && pkDataList.size() > 0) {
					pkData = pkDataList.get(i);
				}
				
				resultWriter.object();
					resultWriter.key("segmentId").value(indexData.getSegmentId());
					
					resultWriter.key("primaryKeys").object();
					if(pkData != null){
						String[][] pkFieldData = pkData.getFieldData();
						for(int k = 0; k < pkFieldData.length; k++) {
							resultWriter.key(pkFieldData[k][0]).value(pkFieldData[k][1]);
						}
					}
					resultWriter.endObject();
					
					resultWriter.key("row").object();
					String[][] fieldData = indexData.getFieldData();
					String[][] analyzedFieldData = analyzedData.getFieldData();
					for(int k = 0; k < fieldData.length; k++) {
						resultWriter.key(fieldData[k][0]).value(fieldData[k][1]);
						resultWriter.key(analyzedFieldData[k][0]+"-ANALYZED").value(analyzedFieldData[k][1]);
					}
					resultWriter.key("isDeleted").value(isDeletedList.get(i));
					resultWriter.endObject();
				resultWriter.endObject();
			}
			resultWriter.endArray();
		}

		resultWriter.endObject();
		resultWriter.done();
		
	}

}
