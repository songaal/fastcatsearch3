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

import java.io.IOException;

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryParseException;
import org.fastcatsearch.ir.query.QueryParser;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.ServiceException;


public class DocumentListJob extends Job {
	
	private String GROUP_SEPARATOR = "(?<!\\\\)&";
	private String VALUE_SEPARATOR = "(?<!\\\\)=";
	
	@Override
	public JobResult doRun() throws JobException, ServiceException {
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
			throw new JobException("[Query Parsing Error] "+e.getMessage());
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
//			logger.debug("NoCache => "+noCache+" ,option = "+q.getMeta().option()+", "+(q.getMeta().option() & Query.SEARCH_OPT_NOCACHE));
			String cacheKey = collection+":"+start+":"+rows;
			if(!noCache){
				result = IRService.getInstance().documentCache().get(cacheKey);
			}
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = IRService.getInstance().getCollectionHandler(collection);
				
				if(collectionHandler == null){
					throw new JobException("## collection ["+collection+"] is not exist!");
				}
				
				result = collectionHandler.listDocument(collection, start, rows);
				
				if(!noCache){
					IRService.getInstance().documentCache().put(cacheKey, result);
				}
			}
//			long st = System.currentTimeMillis();
			

			
			return new JobResult(result);
			
		} catch (IRException e) {
			throw new JobException(e);
		} catch (SettingException e) {
			throw new JobException(e);
		} catch (IOException e) {
			throw new JobException(e);
		} catch(Exception e){
			throw new JobException(e);
		}
		
	}

}

