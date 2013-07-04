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

package org.fastcatsearch.job;

import java.util.Map;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.statistics.StatisticsInfoService;


public class SearchJob extends Job {
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		long st = System.currentTimeMillis();
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		
		
		StatisticsInfoService statisticsInfoService = ServiceManager.getInstance().getService(StatisticsInfoService.class);
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			if(statisticsInfoService.isEnabled()){
				statisticsInfoService.addSearchHit();
				long searchTime = System.currentTimeMillis() - st;
				logger.debug("fail searchTime ={}",searchTime);
				statisticsInfoService.addFailHit();
				statisticsInfoService.addSearchTime(searchTime);
			}
			throw new FastcatSearchException("[Query Parsing Error] "+e.getMessage());
		}
		
		Metadata meta = q.getMeta();
		Map<String, String> userData = meta.userData();
		String keyword = null;
		if(userData != null)
			keyword = userData.get("keyword");
		
		String collection = meta.collectionId();
		if(statisticsInfoService.isEnabled()){
			statisticsInfoService.addSearchHit();
			statisticsInfoService.addSearchHit(collection);
		}
//		logger.debug("collection = "+collection);
		try {
			Result result = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0)
				noCache = true;
//			logger.debug("NoCache => "+noCache+" ,option = "+q.getMeta().option()+", "+(q.getMeta().option() & Query.SEARCH_OPT_NOCACHE));
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			if(!noCache){
				result = irService.searchCache().get(queryString);
			}
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.getCollectionHandler(collection);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collection);
				}
				
				result = collectionHandler.search(q);
				
				irService.searchCache().put(queryString, result);
			}
//			long st = System.currentTimeMillis();
			
			if(keyword != null){
				if(result.getCount() > 0){
					KeywordService.getInstance().addKeyword(keyword);
				}else{
					KeywordService.getInstance().addFailKeyword(keyword);
				}
				statisticsInfoService.addSearchKeyword(keyword);
			}
//			if(result.getCount() > 0 && keyword != null){
//				KeywordService.getInstance().addKeyword(keyword);
//			}

			long searchTime = System.currentTimeMillis() - st;
			if(statisticsInfoService.isEnabled()){
				statisticsInfoService.addSearchTime(searchTime);
				statisticsInfoService.addSearchTime(collection, searchTime);
			}
			return new JobResult(result);
			
		} catch(Exception e){
			if(statisticsInfoService.isEnabled()){
				long searchTime = System.currentTimeMillis() - st;
				//통합 통계
				statisticsInfoService.addFailHit();
				statisticsInfoService.addSearchTime(searchTime);
				if(keyword != null){
					statisticsInfoService.addSearchKeyword(keyword);
				}
				
				//컬렉션별 통계
				statisticsInfoService.addFailHit(collection);
				statisticsInfoService.addSearchTime(collection, searchTime);
			}
//			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00556", e, collection);
		}
		
	}

}

