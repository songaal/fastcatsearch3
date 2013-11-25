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

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;


public class DocumentListJob extends Job {
	
	private String GROUP_SEPARATOR = "(?<!\\\\)&";
	private String VALUE_SEPARATOR = "(?<!\\\\)=";
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		
		int start = 1;
		int rows = 10;
		String collection = null;
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
			if ("cn".equals(type)) {
				collection = value;
			}else if("sn".equals(type)){
				start = Integer.parseInt(value)-1;
			}else if("ln".equals(type)){
				rows = Integer.parseInt(value);
			}
		}
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("[Query Parsing Error] "+e.getMessage());
		} 
		
		Metadata meta = q.getMeta();
//		String collection = meta.collectionName();
//		logger.debug("collection = "+collection);
		try {
			Result result = null;
			boolean noCache = false;
			//no cache 옵션이 없으면 캐시를 확인한다.
//			if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0)
//				noCache = true;
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
//			logger.debug("NoCache => "+noCache+" ,option = "+q.getMeta().option()+", "+(q.getMeta().option() & Query.SEARCH_OPT_NOCACHE));
			String cacheKey = collection+":"+start+":"+rows;
			if(!noCache){
				result = irService.documentCache().get(cacheKey);
			}
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.collectionHandler(collection);
				
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collection);
				}
				
				
				//FIXME
//				result = collectionHandler.searcher().listDocument(collection, start, rows);
				
				if(!noCache){
					irService.documentCache().put(cacheKey, result);
				}
			}
//			long st = System.currentTimeMillis();
			

			
			return new JobResult(result);
			
		} catch(Exception e){
			throw new FastcatSearchException("ERR-00553", e, collection);
		}
		
	}

}

