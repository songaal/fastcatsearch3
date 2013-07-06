package org.fastcatsearch.job.internal;

import java.io.IOException;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableGroupData;

public class InternalGroupSearchJob extends StreamableJob {
	private String query;
	
	public InternalGroupSearchJob(){}
	public InternalGroupSearchJob(String query){
		this.query = query;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String queryString = query;
		
		Query q = null;
		String collection = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("[Query Parsing Error] "+e.getMessage());
		}
		
		Metadata meta = q.getMeta();
		
		collection = meta.collectionId();
		
		try {
			GroupData result = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0)
				noCache = true;
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			if(!noCache)
				result = irService.groupingDataCache().get(queryString);
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.getCollectionHandler(collection);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collection);
				}
				
				result = collectionHandler.searcher().doGrouping(q);
				if(!noCache){
					irService.groupingDataCache().put(queryString, result);
				}
			}
			
			return new JobResult(new StreamableGroupData(result));
			
		} catch (FastcatSearchException e){
			throw e;
		} catch(Exception e){
			logger.error("", e);
//			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00551", e, collection);
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
