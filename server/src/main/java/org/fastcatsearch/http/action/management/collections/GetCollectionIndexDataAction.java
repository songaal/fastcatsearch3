package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;

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
import org.fastcatsearch.job.management.GetCollectionIndexDataJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.vo.CollectionIndexData;
import org.fastcatsearch.vo.CollectionIndexData.RowData;

@ActionMapping(value = "/management/collections/index-data", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetCollectionIndexDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		int start = Integer.parseInt(request.getParameter("start", "0"));
		int end = Integer.parseInt(request.getParameter("end", "0"));

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();

		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);

		GetCollectionIndexDataJob job = new GetCollectionIndexDataJob(collectionId, start, end);
		ResultFuture resultFuture = nodeService.sendRequest(indexNode, job);

		CollectionIndexData data = null;

		if (resultFuture != null) {
			Object obj = resultFuture.take();
			if (obj instanceof CollectionIndexData) {
				data = (CollectionIndexData) obj;
			}
		}

		resultWriter.object()
		.key("collectionId").value(collectionId);
		if (data == null) {
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
			for(RowData rowData : data.getIndexData()) {
				resultWriter.object();
					resultWriter.key("segmentId").value(rowData.getSegmentId());
					resultWriter.key("row").object();
					String[][] fieldData = rowData.getFieldData();
					for(int i = 0; i < fieldData.length; i++) {
						resultWriter.key(fieldData[i][0]).value(fieldData[i][1]);
					}
					resultWriter.endObject();
				resultWriter.endObject();
			}
			resultWriter.endArray();
		}

		resultWriter.endObject();
		resultWriter.done();

	}

}
