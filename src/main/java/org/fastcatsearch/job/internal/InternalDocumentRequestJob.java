package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.vo.StreamableDocumentList;

public class InternalDocumentRequestJob extends StreamableJob {
	
	private String collectionId;
	private int[] docIdList;
	private int length;
	
	public InternalDocumentRequestJob(){}
	
	public InternalDocumentRequestJob(String collectionId, int[] docIdList, int length) {
		this.collectionId = collectionId;
		this.docIdList = docIdList;
		this.length = length;
	}
	
	@Override
	public JobResult doRun() throws JobException, ServiceException {
		
		try {
			CollectionHandler collectionHandler = IRService.getInstance().getCollectionHandler(collectionId);
			
			if(collectionHandler == null){
				throw new JobException("## collection ["+collectionId+"] is not exist!");
			}
			
			List<Document> documentList = collectionHandler.requestDocument(docIdList);
			
			return new JobResult(new StreamableDocumentList(documentList));
			
		} catch(Exception e){
			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new JobException(e);
		}
		
	}
	@Override
	public void readFrom(StreamInput input) throws IOException {
		collectionId = input.readString();
		length = input.readVInt();
		docIdList = new int[length];
		for (int i = 0; i < length; i++) {
			docIdList[i] = input.readVInt();
		}
	}
	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeVInt(length);
		for (int i = 0; i < length; i++) {
			output.writeVInt(docIdList[i]);
		}
	}
}
