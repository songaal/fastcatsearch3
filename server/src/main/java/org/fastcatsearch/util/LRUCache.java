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

package org.fastcatsearch.util;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * memory low상황에서 회수되는 메모리를 사용하는 LRU캐시. 새로운 entry를 입력테스트시 1만건 입력에 600ms~1500ms의 시간소요. 검색엔진의 qps가 500이라고 가정하면 최대 75ms이 소요되므로 병목문제는 없음.
 * 메모리도 60mb이내로 메모리 원활.
 * */
public class LRUCache<K, V> {

	/*
	 * CachedEntry는 soft ref이므로 memory low 상황에서 gc대상이된다. 내부 필드 CacheKey<K>는 Queue에 strong ref로 잡혀있지만,
	 * CachedEntry 객체 자체는 gc가 되고, CacheKey 객체는 유지 된다. 
	 * */
	private Queue<CacheKey<K>> lruQueue;
	private Map<CacheKey<K>, SoftReference<CachedEntry<CacheKey<K>, V>>> map;
	private ReentrantLock lock = new ReentrantLock();
	private int maxCacheSize;
	
	public LRUCache(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
		lruQueue = new PriorityBlockingQueue<CacheKey<K>>(maxCacheSize);
		map = new ConcurrentHashMap<CacheKey<K>, SoftReference<CachedEntry<CacheKey<K>, V>>>(maxCacheSize, 1.0f);
	}


	public void close() {
		map.clear();
		lruQueue.clear();
		map = null;
		lruQueue = null;
	}

	public void put(K key, V value) {
		if (map == null) {
			return;
		}
		lock.lock();
		try{
			if (value != null) {
				if (map.size() >= maxCacheSize || lruQueue.size() >= maxCacheSize) {
					// old를 지운다.
					CacheKey<K> cacheKey = lruQueue.poll();
					if (cacheKey != null) {
						map.remove(cacheKey);
					}
				}
				CacheKey<K> cacheKey = new CacheKey<K>(key);
				CachedEntry<CacheKey<K>, V> e = new CachedEntry(cacheKey, value, map, lruQueue);
				SoftReference<CachedEntry<CacheKey<K>, V>> ref = new SoftReference<CachedEntry<CacheKey<K>, V>>(e);
				SoftReference<CachedEntry<CacheKey<K>, V>> oldRef = map.put(cacheKey, ref);

				if (oldRef != null) {
					if (oldRef.get() != null) {
						lruQueue.remove(oldRef.get().key());
					}
					oldRef.clear();
				}
				lruQueue.offer(cacheKey);
//				logger.debug("map[{}], Q[{}]", map.size(), lruQueue.size());
			}
		}finally{
			lock.unlock();
		}
	}

	public V get(K key) {
		CacheKey<K> cacheKey = new CacheKey<K>(key);
//		lock.lock();
		try{
			if(map.containsKey(cacheKey)) {
				SoftReference<CachedEntry<CacheKey<K>, V>> ref = map.get(cacheKey);
				if (ref != null) {
					CachedEntry<CacheKey<K>, V> entry = ref.get();
					if (entry != null) {
						
						if(lruQueue.remove(cacheKey)){
							//시간 업데이트.
							lruQueue.offer(cacheKey);
							//boolean b = lruQueue.offer(cacheKey);
							//logger.debug("get update {} > {}", b, cacheKey);
						}
						// 아직 회수안된 객체이다.
						return entry.value();
					}
				}
			}
		}finally{
//			lock.unlock();
		}
		return null;
	}

	public int size() {
//		logger.debug("m[{}], q[{}]", map.size(), lruQueue.size());
		return map.size();
	}

}
class CacheKey<K> implements Comparable<CacheKey<K>> {
	private K key;
	private long lastUsedTime;
	
	public CacheKey(K key){
		this.key = key;
		lastUsedTime = System.nanoTime();
	}
	
	@Override
	public String toString(){
		return key.toString() + "[" + lastUsedTime +"]";
	}
	public long lastUsedTime() {
		return lastUsedTime;
	}
	
	@Override
	public int compareTo(CacheKey<K> o) {
		if (lastUsedTime - o.lastUsedTime < 0) {
			return -1;
		} else if (lastUsedTime - o.lastUsedTime > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object o){
		CacheKey<K> e = (CacheKey<K>) o;
		return key.equals(e.key);
	}
	
	@Override
	public int hashCode(){
		return key.hashCode();
	}
	
}
class CachedEntry<CK, V> {
	protected static final Logger logger = LoggerFactory.getLogger(CachedEntry.class);

	private CK key;
	private V value;
	private Queue<CK> lruQueue;
	private Map<CK, SoftReference<CachedEntry<CK, V>>> ref;

	public CachedEntry(CK key, V value, Map<CK, SoftReference<CachedEntry<CK, V>>> ref, Queue<CK> lruQueue) {
		this.key = key;
		this.value = value;
		this.ref = ref;
		this.lruQueue = lruQueue;
	}

	public CK key() {
		return key;
	}

	public V value() {
		return value;
	}


	/*
	 * soft reference의 메모리 회수시 각 map과 q에서도 자신을 삭제하도록 한다.
	 */
	@Override
	public void finalize() {
		Object o = ref.remove(key);
		boolean b = lruQueue.remove(key);
//		logger.debug("finalize {} > map[{}], q[{}], {}, {}", key.toString(), ref.size(), lruQueue.size(), o, b);
	}

	@Override
	public String toString() {
		return key.toString() + ">" + value.toString();
	}

}
