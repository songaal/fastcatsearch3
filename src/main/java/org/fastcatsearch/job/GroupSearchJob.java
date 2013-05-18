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


import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.exception.FastcatSearchException;


public class GroupSearchJob extends Job {
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("[Query Parsing Error] "+e.getMessage());
		}
		
		Metadata meta = q.getMeta();
		Map<String, String> userData = meta.userData();
		String keyword = null;
		if(userData != null)
			keyword = userData.get("keyword");
		
		String collection = meta.collectionName();
		try {
			GroupResults groupResults = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0)
				noCache = true;
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			if(!noCache)
				groupResults = irService.groupingCache().get(queryString);
			
			//Not Exist in Cache
			if(groupResults == null){
				CollectionHandler collectionHandler = irService.getCollectionHandler(collection);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("## collection ["+collection+"] is not exist!");
				}
				
				GroupData groupData = collectionHandler.doGrouping(q);
				Groups groups =q.getGroups();
				groupResults = groups.getGroupResultsGenerator().generate(groupData);
				if(groupResults != null){
					irService.groupingCache().put(queryString, groupResults);
					logger.debug("CACHE_PUT result>>{}, qr >>{}", groupResults, queryString);
				}
			}
			
			if(keyword != null){
				if(groupResults.totalSearchCount() > 0){
					KeywordService.getInstance().addKeyword(keyword);
				}else{
					KeywordService.getInstance().addFailKeyword(keyword);
				}
			}
			
			return new JobResult(groupResults);
			
		} catch(Exception e){
//			EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00555", e);
		}
		
	}

}

