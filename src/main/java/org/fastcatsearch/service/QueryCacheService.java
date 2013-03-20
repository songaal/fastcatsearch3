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

package org.fastcatsearch.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.fastcatsearch.ir.query.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryCacheService extends AbstractService{
	private static QueryCacheService instance = new QueryCacheService();
	
	private LinkedHashMap<String, Result> LRUCache;
	private static final int MAX_CACHE_SIZE = 1024;
	private float loadFactor = 1f;

	private QueryCacheService(){
		LRUCache = new LinkedHashMap<String, Result>(MAX_CACHE_SIZE, loadFactor, true) {
			private static final long serialVersionUID = 4515949078102499045L;
			@Override protected boolean removeEldestEntry (Map.Entry<String, Result> eldest) {
		         return size() > MAX_CACHE_SIZE; 
			}
		}; 
	}

	protected boolean shutdown0() throws ServiceException {
		LRUCache.clear();
		return true;
	}
	
	protected boolean start0() throws ServiceException {
		return true;
	}
	
	public static QueryCacheService getInstance() {
		return instance;
	}
	
	public void put(String queryString, Result result) {
		LRUCache.put(queryString, result);
	}
	
	public Result get(String queryString) {
		return LRUCache.get(queryString);
	}

	public int size(){
		return LRUCache.size();
	}
}
