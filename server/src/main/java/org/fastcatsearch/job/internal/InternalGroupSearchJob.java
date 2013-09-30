package org.fastcatsearch.job.internal;

import java.io.IOException;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableGroupsData;

public class InternalGroupSearchJob extends StreamableJob {
	private QueryMap queryMap;
	
	public InternalGroupSearchJob(){}
	
	public InternalGroupSearchJob(QueryMap queryMap){
		this.queryMap = queryMap;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryMap);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("ERR-01000", e.getMessage());
		}
		
//		Metadata meta = q.getMeta();
		String collectionId = queryMap.collectionId();
		String shardId = queryMap.shardId();
		
		try {
			GroupsData result = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0)
				noCache = true;
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			if(!noCache)
				result = irService.groupingDataCache().get(queryMap.queryString());
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collectionId);
				}
				ShardHandler shardHandler = collectionHandler.getShardHandler(shardId);
				if(shardHandler == null){
					throw new FastcatSearchException("ERR-00521", shardId);
				}
				
				result = shardHandler.searcher().doGrouping(q);
				if(!noCache){
					irService.groupingDataCache().put(queryMap.queryString(), result);
				}
			}
			
			return new JobResult(new StreamableGroupsData(result));
			
		} catch (FastcatSearchException e){
			throw e;
		} catch(Exception e){
			logger.error("", e);
//			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00551", e, collectionId);
		}
		
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		this.queryMap = new QueryMap();
		queryMap.readFrom(input);
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		queryMap.writeTo(output);
	}
}
