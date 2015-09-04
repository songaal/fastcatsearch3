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

import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.error.ServerErrorCode;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;


public class DocumentSearchJob extends Job {
	
	private String GROUP_SEPARATOR = "(?<!\\\\)&";
	private String VALUE_SEPARATOR = "(?<!\\\\)=";
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		String idStr = null;
		String collectionName = null;
		//TODO: parsing type=search:pk,type=list:page
		String[] groups = queryString.split(GROUP_SEPARATOR);
		Query query = new Query();
		query.setMeta(new Metadata());
		for (int i = 0; i < groups.length; i++) {
			String[] tmp = groups[i].split(VALUE_SEPARATOR);
			if(tmp.length < 2){
				logger.debug("Skip parsing = "+groups[i]);
				continue;
			}
			String type = tmp[0];
			String value= tmp[1];
			if ("id".equals(type)) {
				idStr = value;
			}else if("cn".equals(type)){
				collectionName = value;
			}
		}
		
		Query q = QueryParser.getInstance().parseQuery(queryString);

		Metadata meta = q.getMeta();
		String collectionId = meta.collectionId();
//		logger.debug("collection = "+collection);
        Result result = null;
        boolean noCache = false;
        //no cache 옵션이 없으면 캐시를 확인한다.
        if(q.getMeta().isSearchOption(Query.SEARCH_OPT_NOCACHE)){
            noCache = true;
        }
//			logger.debug("NoCache => "+noCache+" ,option = "+q.getMeta().option()+", "+(q.getMeta().option() & Query.SEARCH_OPT_NOCACHE));
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        String cacheKey = collectionId+":"+idStr;
        if(!noCache){
            result = irService.documentCache().get(cacheKey);
        }

        //Not Exist in Cache
        if(result == null){
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);

            if(collectionHandler == null){
                throw new SearchError(ServerErrorCode.COLLECTION_NOT_FOUND, collectionId);
            }

            //FIXME
//				result = collectionHandler.searcher().findDocument(collectionName, idStr);

            if(!noCache){
                irService.documentCache().put(cacheKey, result);
            }
        }
//			long st = System.currentTimeMillis();



        return new JobResult(result);
			

	}

}

