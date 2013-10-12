package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.search.SegmentSearcher;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/index-data")
public class GetCollectionIndexDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String shardId = request.getParameter("shardId");
		int start = Integer.parseInt(request.getParameter("start", "0"));
		int end = Integer.parseInt(request.getParameter("end", "0"));
		int realStart = 0;
		int realEnd = 0;
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		ShardHandler shardHandler = irService.collectionHandler(collectionId).getShardHandler(shardId);
		int segmentSize = shardHandler.segmentSize();

		//TODO 여러세그먼트에 걸쳐있을 경우를 고려한다.
		
		
		
		SegmentReader segmentReader = null;
		for (int segmentNumber = segmentSize - 1; segmentNumber >= 0; segmentNumber--) {
			SegmentReader reader = shardHandler.segmentReader(segmentNumber);
			SegmentInfo segmentInfo = reader.segmentInfo();
			int baseNumber = segmentInfo.getBaseNumber();
			if (start >= baseNumber) {
				logger.debug("selected {}", segmentInfo);
				RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
				int documentCount = revisionInfo.getDocumentCount();
				logger.debug("documentCount {}", documentCount);
				segmentReader = reader;
				
				realStart = start - baseNumber;
				realEnd = end - baseNumber;
				if (realEnd > documentCount) {
					realEnd = documentCount - 1;
				}
				logger.debug("realStart={}, realEnd={}", realStart, realEnd);
			}
		}

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		resultWriter.object()
		.key("shardId").value(shardId)
		.key("indexData").array();
		
		if (segmentReader != null) {
			SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();
			for (int docNo = realStart; docNo <= realEnd; docNo++) {
				
				resultWriter.object()
					.key("segmentId").value(0)
					.key("revisionId").value(0)
					.key("row").object();
				
				Document document = segmentSearcher.getDocument(docNo);
				int fieldSize = document.size();
				for (int index = 0; index < fieldSize; index++) {
					Field field = document.get(index);
					resultWriter.key(field.getId()).value(field.toString());
				}
				resultWriter.endObject().endObject();
			}
		} else {
			logger.debug("segmentReader is NULL");
		}
		
		resultWriter.endArray()
		.endObject();
		
		resultWriter.done();
	}

}
