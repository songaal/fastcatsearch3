/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.io.cache;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LRUBlockCache {
	private static Logger logger = LoggerFactory.getLogger(LRUBlockCache.class);
			
	private static AtomicInteger totalCacheSize = new AtomicInteger();
	private CachedBlock head; //circular linked list
	private CachedBlock[] dataList;
	private int bucketSize;
//	private static int BLOCK_SIZE = IOUtil.FILEBLOCKSIZE;
	private int count;
	private int limitSize;
	
	public LRUBlockCache(){
		bucketSize = 1024;
		dataList = new CachedBlock[bucketSize];
		head = new CachedBlock();
		count = 0;
		limitSize = 1024;
	}
	
	public synchronized CachedBlock getBlock(int block){
		int key = block & (bucketSize - 1);
//		key = block % bucketSize;
		CachedBlock data = null;
		CachedBlock prev = null;
		
		for(data = dataList[key]; data != null && data.block != block; prev = data, data = data.next){
		}
			
		if(data == null){
			return null;
		}
		
		//assert data.pos == pos
		//data는 중간에서 빠진다.
		if(prev != null)
			prev.next = data.next;
		
		//data는 맨앞으로 이동한다.
		if(dataList[key] != data){
			CachedBlock tmp = dataList[key];
			dataList[key] = data;
			dataList[key].next = tmp;
		}
		
		data.before.after = data.after;
		CachedBlock tmp = head.after;
		
		head.after = data;
		data.before = head;
		
		data.after = tmp;
		tmp.before = data;
		
		return data;
		
	}
	
	public synchronized void putBlock(CachedBlock newData){
		
		int key = newData.block & (bucketSize - 1);
		
		//logger.info("block = "+newData.block+", key="+key+", newData="+newData+", count="+count+", mem="+Runtime.getRuntime().totalMemory());
		System.out.println(dataList.length+", "+bucketSize);
		CachedBlock data = dataList[key];
		if(data == null){
			dataList[key] = newData;
		}else{
			dataList[key] = newData;
			dataList[key].next = data;
		}
		
		//circular list
		//head before put!!
		if (count == 0) {
			head.before = newData;
			head.after = newData;
			newData.after = head;
			newData.before = head;
		}else{
			CachedBlock temp = head.before;
			head.before = newData;
			newData.after = head;
			newData.before = temp;
			temp.after = newData;
		}
		
		
		count++;
		totalCacheSize.getAndAdd(1);
		
		this.check();
	}
	
	public synchronized void reduce(){
		int targetCount = (int) (count * 0.9);
		logger.debug("Reduce {}", targetCount);
		for (; count > targetCount; count--) {
			CachedBlock data = head.after;
			if(data != null){
				data.remove();
				totalCacheSize.getAndAdd(-1);
				System.out.println(totalCacheSize.get()+">>"+dataList.length);
			}
		}
	}

	public void rehash(){
		//TODO
		int newBucketSize = (int) (bucketSize * 1.5);
		System.out.println("REHASH to "+newBucketSize);
		CachedBlock[] newDataList = new CachedBlock[newBucketSize];
		
		for (int i = 0; i < dataList.length; i++) {
			CachedBlock data = dataList[i];
			while (data != null) {
				int hashPos = data.block & (bucketSize - 1);
				CachedBlock nextData = data.next;
				data.next = newDataList[hashPos];
				newDataList[hashPos] = data;
				data = nextData;
			}
		} 
		
		bucketSize = newBucketSize;
		dataList = newDataList;
	    
	}
	
	public int size(){
		int size = 0;
		while(head.next != null){
			size += head.after.size();
		}
		
		return size;
	}
	public void check() {
		
		if (totalCacheSize.get() >= this.getLimitSize()) {
			this.reduce();
		}
		if (count >= bucketSize) {
			this.rehash();
		}
	}
	
	public int getLimitSize() {
		return limitSize;
	}

	public void setLimitSize(int limitSize) {
		this.limitSize = limitSize;
	}
}
