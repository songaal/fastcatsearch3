package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.ArrayList;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.search.SegmentSearcher;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/index-data")
public class GetCollectionIndexDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		int start = Integer.parseInt(request.getParameter("start", "0"));
		int end = Integer.parseInt(request.getParameter("end", "0"));
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		
		CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
		if(collectionHandler == null || !collectionHandler.isLoaded()){
			resultWriter.object()
			.key("collectionId").value(collectionId)
			.key("documentSize").value(0)
			.key("fieldList").array().endArray()
			.key("indexData").array().endArray()
			.endObject();
			
			resultWriter.done();
			return;
		}
		int documentSize = collectionHandler.collectionContext().dataInfo().getDocuments();
		
		
		int segmentSize = collectionHandler.segmentSize();
		//이 배열의 index번호는 세그먼트번호.
		int[] segmentEndNumbers = new int[segmentSize];
		for (int segmentNumber = 0; segmentNumber < segmentSize; segmentNumber++) {
			SegmentReader reader = collectionHandler.segmentReader(segmentNumber);
			SegmentInfo segmentInfo = reader.segmentInfo();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			segmentEndNumbers[segmentNumber] = segmentInfo.getBaseNumber() + revisionInfo.getDocumentCount() - 1;
		}
		
		//여러세그먼트에 걸쳐있을 경우를 고려한다.
		int[][] matchSegmentList = matchSegment(segmentEndNumbers, start, end - start + 1);

		

		resultWriter.object()
		.key("collectionId").value(collectionId)
		.key("documentSize").value(documentSize);
		//write field list
		resultWriter.key("fieldList").array();
		if(matchSegmentList.length > 0){
			int segmentNumber = matchSegmentList[0][0];
			int startNo = matchSegmentList[0][1];
			SegmentReader segmentReader = collectionHandler.segmentReader(segmentNumber);
			SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();
			Document headerDocument = segmentSearcher.getDocument(startNo);
			for (int index = 0; index < headerDocument.size(); index++) {
				Field field = headerDocument.get(index);
				resultWriter.value(field.getId());
			}
		}
		resultWriter.endArray();
		
		//write data
		resultWriter.key("indexData").array();
		for (int i = 0; i < matchSegmentList.length; i++) {
			int segmentNumber = matchSegmentList[i][0];
			int startNo = matchSegmentList[i][1];
			int endNo = matchSegmentList[i][2];
			
			SegmentReader segmentReader = collectionHandler.segmentReader(segmentNumber);
			
			if (segmentReader != null) {
				SegmentInfo segmentInfo = segmentReader.segmentInfo();
				String segmentId = segmentInfo.getId();
				SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();
				
				for (int docNo = startNo; docNo <= endNo; docNo++) {
					
					Document document = segmentSearcher.getDocument(docNo);
					if(document == null){
						//문서의 끝에 다다름.
						break;
					}
					
					resultWriter.object()
						.key("segmentId").value(segmentId)
						.key("row").object();
					
					
					
					int fieldSize = document.size();
					logger.debug("document >> {}", document);
					for (int index = 0; index < fieldSize; index++) {
						Field field = document.get(index);
						
						resultWriter.key(field.getId()).value(field.toString());
					}
					resultWriter.endObject().endObject();
				}
			} else {
				logger.debug("segmentReader is NULL");
			}
		}
		
		resultWriter.endArray()
		.endObject();
		
		resultWriter.done();
	}
	
	private int[][] matchSegment(int[] segEndNums, int start, int rows) {
		// [][세그먼트번호,시작번호,끝번호]
		ArrayList<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < segEndNums.length; i++) {
			if (start > segEndNums[i]) {
				start = start - segEndNums[i] - 1;
			} else {
				int[] res = new int[3];
				int emptyCount = segEndNums[i] - start + 1;
				res[0] = i;// 세그먼트번호
				if (emptyCount < rows) {
					res[1] = start;// 시작번호
					res[2] = segEndNums[i];
					start = 0;
					rows = rows - emptyCount;
					list.add(res);
				} else {
					res[1] = start;// 시작번호
					res[2] = start + rows - 1;// 끝번호
					list.add(res);
					break;
				}
			}
		}
		int[][] result = new int[list.size()][3];
		for (int i = 0; i < list.size(); i++) {
			int[] tmp = list.get(i);
			result[i][0] = tmp[0];
			result[i][1] = tmp[1];
			result[i][2] = tmp[2];
		}

		return result;
	}

}
