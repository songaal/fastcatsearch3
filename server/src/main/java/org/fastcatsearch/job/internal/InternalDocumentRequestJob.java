package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableDocumentList;

/*
 * 문서의 모든 필드를 통째로 가져오는 job.
 *
 * */
public class InternalDocumentRequestJob extends StreamableJob {
	
	private String collectionId;
	private String shardId;
	private int[] docIdList;
	private int length;
	
	public InternalDocumentRequestJob(){}
	
	public InternalDocumentRequestJob(String collectionId, String shardId, int[] docIdList, int length) {
		this.collectionId = collectionId;
		this.docIdList = docIdList;
		this.length = length;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			
			if(collectionHandler == null){
				throw new FastcatSearchException("ERR-00520", collectionId);
			}
			
			ShardHandler shardHandler = collectionHandler.getShardHandler(shardId);
			List<Document> documentList = shardHandler.searcher().requestDocument(docIdList);
			
			return new JobResult(new StreamableDocumentList(documentList));
		} catch (FastcatSearchException e){
			throw e;
		} catch(Exception e){
//			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00550", e, collectionId);
		}
		
	}
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		length = input.readVInt();
		docIdList = new int[length];
		for (int i = 0; i < length; i++) {
			docIdList[i] = input.readVInt();
		}
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeVInt(length);
		for (int i = 0; i < length; i++) {
			output.writeVInt(docIdList[i]);
		}
	}
}
