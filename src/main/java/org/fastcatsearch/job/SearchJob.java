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

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryParseException;
import org.fastcatsearch.ir.query.QueryParser;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.QueryCacheService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.statistics.StatisticsInfoService;


public class SearchJob extends Job {
	
	@Override
	public JobResult doRun() throws JobException, ServiceException {
		long st = System.currentTimeMillis();
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		
		StatisticsInfoService statisticsInfoService = StatisticsInfoService.getInstance();
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
			throw new JobException("[Query Parsing Error] "+e.getMessage());
		}
		
		Metadata meta = q.getMeta();
		Map<String, String> userData = meta.userData();
		String keyword = null;
		if(userData != null)
			keyword = userData.get("keyword");
		
		String collection = meta.collectionName();
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
			
			if(!noCache)
				result = QueryCacheService.getInstance().get(queryString);
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = IRService.getInstance().getCollectionHandler(collection);
				
				if(collectionHandler == null){
					throw new JobException("## collection ["+collection+"] is not exist!");
				}
				
				result = collectionHandler.search(q);
				
				if(!noCache)
					QueryCacheService.getInstance().put(queryString, result);
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
			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new JobException(e);
		}
		
	}

	public static void main(String[] args) throws JobException, ServiceException {
		String homePath = args[0];
		String queryString = args[1];
		IRSettings.setHome(homePath);
		
		
		long st = System.currentTimeMillis();
		
		SearchJob job = new SearchJob();
		job.setArgs(new String[]{queryString});
		JobResult obj = job.doRun();
		Result result = null;
		if(obj != null)
			result = (Result)obj.result();
		
		logger.info("search time = "+(System.currentTimeMillis() - st)+" ms");
		logger.info("TotalCount = " + result.getTotalCount());
		int resultCount = result.getCount();
		logger.info("Count = " + resultCount);
		int fieldCount = result.getFieldCount();
		logger.info("FieldCount = " + fieldCount);
		
		Row[] data = result.getData();
		for (int i = 0; i < resultCount; i++) {
			Row row = data[i];
			logger.info(i+"] "+row.toString());
		}
		
		GroupResult[] groupResultList = result.getGroupResult();
		for (int i = 0; i < groupResultList.length; i++) {
			GroupResult groupResult = groupResultList[i];
			logger.info("== Group Result - " + (i+1)+ ", count = "+groupResult.size()+" ==");
			int size = groupResult.size();
			for (int j = 0; j < size; j++) {
				GroupEntry entry = groupResult.getEntry(j);
				logger.info("{} : {}", entry.key.getKeyString(), entry.count());
				
			}
		}
		
	}
}

