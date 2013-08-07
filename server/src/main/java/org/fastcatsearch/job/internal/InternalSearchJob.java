package org.fastcatsearch.job.internal;

import java.io.IOException;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableInternalSearchResult;

public class InternalSearchJob extends StreamableJob {
	private String query;
	
	public InternalSearchJob(){}
	public InternalSearchJob(String query){
		this.query = query;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String queryString = query;
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("ERR-01000", e.getMessage());
		}
		
		Metadata meta = q.getMeta();
		String collection = meta.collectionId();

		try {
			InternalSearchResult result = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0){
				noCache = true;
			}
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			if(!noCache){
				result = irService.shardSearchCache().get(queryString);
			}
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.collectionHandler(collection);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collection);
				}
				
				result = collectionHandler.searcher().searchInternal(q);
				irService.shardSearchCache().put(queryString, result);
			}

			//shard에서는 keyword 통계를 내지않는다.
			logger.debug(">>result : {}", result);
			return new JobResult(new StreamableInternalSearchResult(result));
			
		} catch (FastcatSearchException e){
			throw e;
		} catch(Exception e){
			logger.error("", e);
			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00552", e, collection);
		}
		
	}
	@Override
	public void readFrom(DataInput input) throws IOException {
		query = input.readString();
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(query);
	}
}
