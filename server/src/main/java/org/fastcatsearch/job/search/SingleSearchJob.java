/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SearchResultAggregator;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.ServiceManager;


public class SingleSearchJob extends Job {
	
	private static final long serialVersionUID = -4667914054975750035L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		long st = System.currentTimeMillis();
		QueryMap queryMap = (QueryMap) getArgs();
		
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryMap);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("[Query Parsing Error] "+e.getMessage());
		}
		
		Metadata meta = q.getMeta();
		Map<String, String> userData = meta.userData();
		String keyword = null;
		if(userData != null)
			keyword = userData.get("keyword");
		
		String collectionId = meta.collectionId();
		String[] shardIdList = meta.getSharIdList();
		
//		logger.debug("collection = "+collection);
		try {
			Result result = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if(q.getMeta().isSearchOption(Metadata.SEARCH_OPT_NOCACHE)){
				noCache = true;
			}
//			logger.debug("NoCache => "+noCache+" ,option = "+q.getMeta().option()+", "+(q.getMeta().option() & Query.SEARCH_OPT_NOCACHE));
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			logger.debug(">> qs : {}", queryMap.queryString());
			if(!noCache){
				result = irService.searchCache().get(queryMap.queryString());
			}
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collectionId);
				}
				
				List<InternalSearchResult> resultList = new ArrayList<InternalSearchResult>(shardIdList.length);
				for (int i = 0; i < shardIdList.length; i++) {
					String shardId = shardIdList[i];
					ShardHandler shardHandler =  collectionHandler.getShardHandler(shardId);
					if(shardHandler == null){
						//FIXME code value
						throw new FastcatSearchException("ERR-00520", shardId);
					}
					InternalSearchResult internalSearchResult = shardHandler.searcher().searchInternal(q);
					logger.debug("# internalSearchResult > {}", internalSearchResult);
					resultList.add(internalSearchResult);
					
				}
				CollectionContext collectionContext = collectionHandler.collectionContext();
				
				Schema schema = collectionContext.schema();
				SearchResultAggregator aggregator = new SearchResultAggregator(q, schema);
				InternalSearchResult aggregatedSearchResult = aggregator.aggregate(resultList);
				int totalSize = aggregatedSearchResult.getTotalCount();
				logger.debug("search result : {}", aggregatedSearchResult);
				
				result = new Result();
				irService.searchCache().put(queryMap.queryString(), result);
			}
//			long st = System.currentTimeMillis();
			
			if(keyword != null){
				if(result.getCount() > 0){
					KeywordService.getInstance().addKeyword(keyword);
				}else{
					KeywordService.getInstance().addFailKeyword(keyword);
				}
			}
//			if(result.getCount() > 0 && keyword != null){
//				KeywordService.getInstance().addKeyword(keyword);
//			}

			long searchTime = System.currentTimeMillis() - st;
			return new JobResult(result);
			
		} catch(Exception e){
//			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00556", e, collectionId);
		}
		
	}

}

