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

import java.io.IOException;
import java.util.Map;

import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.error.ServerErrorCode;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;


public class GroupSearchJob extends Job {
	
	private static final long serialVersionUID = 3171817565336360699L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		QueryMap queryMap = (QueryMap) getArgs();
		
		Query q = QueryParser.getInstance().parseQuery(queryMap);

		Metadata meta = q.getMeta();
		Map<String, String> userData = meta.userData();
		String keyword = null;
		if(userData != null)
			keyword = userData.get("keyword");
		
		String collection = meta.collectionId();
			GroupResults groupResults = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
			if(q.getMeta().isSearchOption(Query.SEARCH_OPT_NOCACHE)){
				noCache = true;
			}
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			if(!noCache)
				groupResults = irService.groupingCache().get(queryMap.queryString());
			
			//Not Exist in Cache
			if(groupResults == null){
				CollectionHandler collectionHandler = irService.collectionHandler(collection);
				
				if(collectionHandler == null){
                    throw new SearchError(ServerErrorCode.COLLECTION_NOT_FOUND, collection);
				}

                GroupsData groupData = null;
                try {
                    groupData = collectionHandler.searcher().doGrouping(q);
                } catch (Throwable t) {
                    throw new SearchError(ServerErrorCode.SERVER_SEARCH_ERROR, t.getMessage());
                }
                Groups groups =q.getGroups();
				groupResults = groups.getGroupResultsGenerator().generate(groupData);
				if(groupResults != null){
					irService.groupingCache().put(queryMap.queryString(), groupResults);
				}
			}
			
			if(keyword != null){
				if(groupResults.totalSearchCount() > 0){
				}else{
				}
			}
			
			return new JobResult(groupResults);
			

	}

}

