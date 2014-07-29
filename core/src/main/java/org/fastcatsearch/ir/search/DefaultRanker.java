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

package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.FixedMaxPriorityQueue;

/**
 * 정렬조건이 없을때 최신문서순으로 정렬해주는 랭커이다.
 * docNo가 클수록 최신문서이다. 
 * 이 heap에서 pop한 결과는 역순으로 이용된다.
 * @author swsong
 *
 */
public class DefaultRanker extends FixedMaxPriorityQueue<HitElement>{
	public DefaultRanker(int maxSize){
		super(maxSize);
	}
	
	@Override
	protected int compare(HitElement one, HitElement two) {
		return one.compareTo(two);
//		//최신문서 순으로 보여준다.
//		//최신 세그먼트를 우선으로 보여주고 세그먼트가 같으면 문서번호로 구분한다.
//		if(one.segmentSequence() != two.segmentSequence()){
//			return two.segmentSequence() - one.segmentSequence();
//		}
//		return two.docNo() - one.docNo() ;
	}
	
}
