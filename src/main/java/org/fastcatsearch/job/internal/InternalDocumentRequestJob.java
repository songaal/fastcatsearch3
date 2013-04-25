package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryParseException;
import org.fastcatsearch.ir.query.QueryParser;
import org.fastcatsearch.ir.query.ShardSearchResult;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.statistics.StatisticsInfoService;
import org.fastcatsearch.transport.vo.StreamableDocumentList;
import org.fastcatsearch.transport.vo.StreamableGroupData;
import org.fastcatsearch.transport.vo.StreamableShardSearchResult;

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
		long st = System.currentTimeMillis();
		
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
