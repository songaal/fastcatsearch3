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

package org.fastcatsearch.ir.io;

import org.fastcatsearch.ir.search.HitElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 세그먼트별 검색결과 HitElement 리스트
 * FixedMinHeap에서 사용되려면 Comparable해야하기때문에 형식을 맞추기위해 Comparable구현.
 * 실제로 FixedHitReader의 비교는 HitMerger의 정렬function에 의해 수행됨.
 * @author swsong
 *
 */
public class FixedHitReader implements Comparable<FixedHitReader>{
	protected static Logger logger = LoggerFactory.getLogger(FixedHitReader.class);
	
	private String collection;
	private int shardId;
	
	private HitElement[] list;
	private int head;
	private int tail;
	
	
	public FixedHitReader(HitElement[] list, int head, int tail){
		this(null, -1, list, head, tail);
	}
	
	public FixedHitReader(String collection, int shardId, HitElement[] list, int head, int tail){
		this.collection =collection;
		this.shardId = shardId;
		this.list = list;
		this.head = head - 1;
		this.tail = tail;
	}
	
	public String collection(){
		return collection;
	}
	
	public int shardId(){
		return shardId;
	}
	
	public boolean next(){
		head++;
		
		if(head >= tail)
			return false;
		
		return true;
	}
	
	public HitElement read(){
		if(collection != null){
			list[head].collection(collection);
			list[head].shardId(shardId);
		}
		return list[head];
	}

	public int compareTo(FixedHitReader r) {
		//HitElement는 HitRanker를 통해 비교를 해야하나 정렬을 지정하지 않으면 기본적으로 최신문서를 보여준다.
		HitElement one = read();
		HitElement two = r.read();
		//같은 컬렉션에 같은 shard일 경우에만 비교를 하고 
		//다를 경우는 문서번호가 의미가 없으므로 같음으로 넘긴다.
		if(collection == r.collection && shardId == r.shardId){
			if(two.docNo() == one.docNo()){
			//문서번호가 최신인걸 보여준다. 머징시 문서번호가 큰 순으로 보여주려면 역정렬 필요. 
				return two.docNo() - one.docNo();
			}
		}
		
		return 0;
		
	}

	public void dump(){
		for (int i = head; i < tail; i++) {
			logger.debug("FixedHitReader list-{} >> {}", i, list[i]);
		}
	}
}
