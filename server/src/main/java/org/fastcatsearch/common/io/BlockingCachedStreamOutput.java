/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.fastcatsearch.common.io;


import java.lang.ref.SoftReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BlockingCachedStreamOutput {
	protected static Logger logger = LoggerFactory.getLogger(BlockingCachedStreamOutput.class);
	
	private final int defaultBufferSize;
	private final int maxSize;
	
	private final SoftWrapper<BlockingQueue<Entry>> cache;
    private final AtomicInteger totalCount = new AtomicInteger();
    private final AtomicInteger inPoolCount = new AtomicInteger();
    private final AtomicInteger outPoolCount = new AtomicInteger();
	    
	//100m, 3m+4k
	public BlockingCachedStreamOutput(int maxSize, int defaultBufferSize){
		this.maxSize = maxSize;
		this.defaultBufferSize = defaultBufferSize;
		
		cache = new SoftWrapper<BlockingQueue<Entry>>();
		initCache();
	}
	
	private BlockingQueue<Entry> initCache(){
		logger.debug("initCache maxSize={}", maxSize);
		BlockingQueue<Entry> ref = new LinkedBlockingQueue<Entry>(maxSize);
		cache.set(ref);
		inPoolCount.set(0);
		//발급해준 버퍼는 아직 유효하므로 총 갯수는 발급한 버퍼갯수이다. 
		totalCount.set(outPoolCount.get());
		logger.debug("{}", toString());
		return ref;
	}
	
	public int totalCount(){
		return totalCount.get();
	}
	
	@Override
	public String toString(){
		return "[Cache]mem="+Strings.getHumanReadableByteSize(defaultBufferSize * totalCount.get())+", totalCount="+totalCount.get()+", inPoolCount="+inPoolCount.get()+", outPoolCount="+outPoolCount.get();
	}
	
    private synchronized Entry newEntry() {
    	if(totalCount.get() < maxSize){
	    	logger.debug("make newEntry totalCount={}", totalCount.get());
	    	totalCount.incrementAndGet();
	        BytesStreamOutput bytes = new BytesStreamOutput(defaultBufferSize);
	        return new Entry(bytes);
    	}
    	return null;
    }


    public void clear() {
        cache.clear();
    }
    
//    static AtomicInteger c = new AtomicInteger();
//    static AtomicInteger r = new AtomicInteger();
    
    
    public Entry popEntry() {
//    	logger.debug("{}", toString());
        BlockingQueue<Entry> ref = cache.get();
        if (ref == null) {
        	ref = initCache();
        }
        Entry entry = null;
        while(entry == null){
			try {
				entry = ref.poll();
				
				//가용한 버퍼는 없으나, max갯수까지 여유가 있을 경우는 생성.
				if(entry == null && totalCount.get() < maxSize){
					entry = newEntry();
				}
				//생성을 못했을 경우 take.
				if(entry == null){
//					logger.debug("# Take wait!!");
					entry = ref.take();
//					logger.debug("# Take Done!!");
					entry.reset();
					inPoolCount.decrementAndGet();
				}
				if(entry != null){
					outPoolCount.incrementAndGet();
				}
			} catch (InterruptedException e) {
				logger.debug("interrupt while take");
			}
        }
        return entry;
    }
    
    public void pushEntry(Entry entry) {
        entry.reset();
        
        BlockingQueue<Entry> ref = cache.get();
        if (ref == null) {
        	ref = initCache();
        }
        
        //동기화에 문제가 없다면 항상 offer에 성공해야한다. 
        if(ref.offer(entry)){
        	inPoolCount.incrementAndGet();
        }
        outPoolCount.decrementAndGet();
    }
    
    public static class Entry {
        private final BytesStreamOutput bytes;

        Entry(BytesStreamOutput bytes) {
            this.bytes = bytes;
        }

        public void reset() {
            bytes.reset();
        }

        public BytesStreamOutput bytes() {
            return bytes;
        }
    }

    static class SoftWrapper<T> {
        private SoftReference<T> ref;

        public SoftWrapper() {
        }

        public void set(T ref) {
            this.ref = new SoftReference<T>(ref);
        }

        public T get() {
            return ref == null ? null : ref.get();
        }

        public void clear() {
            ref = null;
        }
    }

}
