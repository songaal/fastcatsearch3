package org.fastcatsearch.job.internal;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;

import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.ShardSearchResult;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableShardSearchResult;

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
		String collection = meta.collectionName();

		try {
			ShardSearchResult result = null;
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
				CollectionHandler collectionHandler = irService.getCollectionHandler(collection);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collection);
				}
				
				result = collectionHandler.searchShard(q);
				irService.shardSearchCache().put(queryString, result);
			}

			//shard에서는 keyword 통계를 내지않는다.
			logger.debug(">>result : {}", result);
			return new JobResult(new StreamableShardSearchResult(result));
			
		} catch (FastcatSearchException e){
			throw e;
		} catch(Exception e){
			logger.error("", e);
			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00552", e, collection);
		}
		
	}
	@Override
	public void readFrom(StreamInput input) throws IOException {
		query = input.readString();
	}
	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(query);
	}
}
