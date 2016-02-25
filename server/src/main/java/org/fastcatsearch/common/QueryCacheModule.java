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

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.LRUCache;

/**
 * */
public class QueryCacheModule<K, V> extends AbstractModule {

	private LRUCache<K, V> lruCache;
	
	public QueryCacheModule(Environment environment, Settings settings) {
		super(environment, settings);
	}
    private int maxCacheSize;

	@Override
	protected boolean doLoad() throws ModuleException {
        maxCacheSize = settings.getInt("search-cache-size", 1000);
        reset();
		return true;
	}

	@Override
	protected boolean doUnload() {
		lruCache.close();
		return true;
	}

    public void reset() {
        LRUCache<K, V> oldLruCache = this.lruCache;
        lruCache = new LRUCache<K, V>(maxCacheSize);
        if(oldLruCache != null) {
            oldLruCache.close();
        }
    }
	public void put(K key, V value) {
		lruCache.put(key, value);
	}

	public V get(K key) {
		return lruCache.get(key);
	}

	public int size() {
		return lruCache.size();
	}
}
