package org.fastcatsearch.job.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.search.SegmentSearcher;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.vo.CollectionIndexData;
import org.fastcatsearch.vo.CollectionIndexData.RowData;

public class GetCollectionIndexDataJob extends Job implements Streamable {

	private static final long serialVersionUID = 1123665008671820737L;
	private String collectionId;
	private int start;
	private int end;
	
	public GetCollectionIndexDataJob() {}
	
	public GetCollectionIndexDataJob(String collectionId, int start, int end) {
		this.collectionId = collectionId;
		this.start = start;
		this.end = end;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
		if(collectionHandler == null || !collectionHandler.isLoaded()){
			
			CollectionIndexData data = new CollectionIndexData(collectionId, 0, null, null);
			return new JobResult(data);
		}
		
		try {
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
	
			List<String> fieldList = new ArrayList<String>();
			//write field list
			if(matchSegmentList.length > 0){
				int segmentNumber = matchSegmentList[0][0];
				int startNo = matchSegmentList[0][1];
				SegmentReader segmentReader = collectionHandler.segmentReader(segmentNumber);
				SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();
				Document headerDocument = segmentSearcher.getDocument(startNo);
				for (int index = 0; index < headerDocument.size(); index++) {
					Field field = headerDocument.get(index);
					fieldList.add(field.getId());
				}
			}
			
			//write data
			List<RowData> indexData = new ArrayList<RowData>();
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
						
						int fieldSize = document.size();
						String[][] fieldData = new String[fieldSize][];
						for (int index = 0; index < fieldSize; index++) {
							Field field = document.get(index);
							fieldData[index] = new String[] { field.getId(), field.toString() };
						}
						RowData rowData = new RowData(segmentId, fieldData);
						indexData.add(rowData);
					}
					
					
				} else {
					logger.debug("segmentReader is NULL");
				}
			}
			
			
			CollectionIndexData data = new CollectionIndexData(collectionId, documentSize, fieldList, indexData);
			return new JobResult(data);
			
			
		} catch (Throwable t) {
			logger.error("", t);
			CollectionIndexData data = new CollectionIndexData(collectionId, 0, null, null);
			return new JobResult(data);
		}
		
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
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		start = input.readInt();
		end = input.readInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeInt(start);
		output.writeInt(end);
	}

}
