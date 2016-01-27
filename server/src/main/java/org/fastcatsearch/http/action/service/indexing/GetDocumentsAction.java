package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.management.GetCollectionIndexDataJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.vo.CollectionIndexData;
import org.fastcatsearch.vo.CollectionIndexData.RowData;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.Writer;
import java.util.List;

@ActionMapping(value = "/service/index", method = { ActionMethod.GET })
public class GetDocumentsAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
        if(collectionId == null || collectionId.length() == 0) {
            throw new FastcatSearchException("collection cannot be empty!");
        }
		int start = Integer.parseInt(request.getParameter("start", "0"));
		int end = Integer.parseInt(request.getParameter("length", "10")) + start - 1;
		String pkValue = request.getParameter("pk");
        boolean includeDelete = request.getBooleanParameter("includeDelete");

        writeHeader(response);
        response.setStatus(HttpResponseStatus.OK);
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

        JobService jobService = ServiceManager.getInstance().getService(JobService.class);
		GetCollectionIndexDataJob job = new GetCollectionIndexDataJob(collectionId, start, end, pkValue);
		ResultFuture resultFuture = jobService.offer(job);

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
			List<RowData> indexDataList = data.getIndexData();
			List<Boolean> isDeletedList = data.getIsDeletedList();
			
			resultWriter.key("documentSize").value(data.getDocumentSize());
            resultWriter.key("deleteSize").value(data.getDeleteSize());
			resultWriter.key("fieldList").array();
			for(String fieldId : data.getFieldList()) {
				resultWriter.value(fieldId);
			}
			resultWriter.endArray();
			
			resultWriter.key("indexData").array();
			for(int i = 0; i < indexDataList.size(); i++) {
				RowData rowData = indexDataList.get(i);
				resultWriter.object();
					resultWriter.key("segmentId").value(rowData.getSegmentId());
					resultWriter.key("row").object();
					String[][] fieldData = rowData.getFieldData();
					for(int k = 0; k < fieldData.length; k++) {
						resultWriter.key(fieldData[k][0]).value(fieldData[k][1]);
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
