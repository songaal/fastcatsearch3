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

package org.fastcatsearch.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.settings.Settings;

public class QueryCacheModule<T> extends AbstractModule {

	private LinkedHashMap<String, T> LRUCache;
	private static final int MAX_CACHE_SIZE = 1024;
	private float loadFactor = 1f;

	public QueryCacheModule(Environment environment, Settings settings) {
		super(environment, settings);
	}

	@Override
	protected boolean doUnload() {
		LRUCache.clear();
		return true;
	}
	@Override
	protected boolean doLoad() {
		LRUCache = new LinkedHashMap<String, T>(MAX_CACHE_SIZE, loadFactor, true) {
			private static final long serialVersionUID = 4515949078102499045L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		};

		return true;
	}


	public void put(String queryString, T result) {
		LRUCache.put(queryString, result);
	}

	public T get(String queryString) {
		return LRUCache.get(queryString);
	}

	public int size() {
		return LRUCache.size();
	}
}
