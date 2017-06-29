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

import java.io.IOException;
import java.util.List;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.DistanceField;
import org.fastcatsearch.ir.field.HitField;
import org.fastcatsearch.ir.field.MatchOrderField;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.Sort;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.sort.SortFunction;


/**
 * 각 세그먼트에서 검색되어 나온 HitElement리스트들을 하나의 최종 결과로 병합하는 클래스 
 * 이 heap에서 pop 한 결과는 순차적으로 이용된다.
 * @see HitRanker HitRanker 세그먼트별 검색결과 랭킹클래스
 * @author swsong
 *
 */
public class HitMerger extends FixedMinHeap<FixedHitReader> {
	private SortFunction[] sortFunctions;
	
	public HitMerger(int segmentSize) {
		super(segmentSize);
	}

	public HitMerger(List<Sort> querySortList, Schema schema, int segmentSize) throws IOException{
		super(segmentSize);
		
		int size = querySortList.size();
		sortFunctions = new SortFunction[size];
		
		for (int i = 0; i < size; i++) {
			Sort sort = querySortList.get(i);
			String fieldIndexId = sort.fieldIndexId();
			int idx = schema.getFieldIndexSequence(fieldIndexId);
			
			////////_hit_ , _score_ 필드의 경우 처리해준다.
			if(idx == -1){
				if(fieldIndexId.equalsIgnoreCase(ScoreField.fieldName)){
					sortFunctions[i] = sort.createSortFunction(ScoreField.field);
				}else if(fieldIndexId.equalsIgnoreCase(HitField.fieldName)){
					sortFunctions[i] = sort.createSortFunction(HitField.field);
                }else if(fieldIndexId.equalsIgnoreCase(DistanceField.fieldName)){
                    sortFunctions[i] = sort.createSortFunction(DistanceField.field);
				}else if(fieldIndexId.equalsIgnoreCase(MatchOrderField.fieldName)){
					sortFunctions[i] = sort.createSortFunction(MatchOrderField.field);
				}else{
					throw new IOException("Unknown sort field name = "+fieldIndexId);
				}
			}else{
				FieldIndexSetting fieldIndexSetting = schema.getFieldIndexSetting(fieldIndexId);
				String refId = fieldIndexSetting.getRef();
				FieldSetting fieldSetting = schema.getFieldSetting(refId);
				sortFunctions[i] = sort.createSortFunction(fieldSetting);
			}
			
			logger.debug("sortFunctions[{}]={}", i, sortFunctions[i]);
		}
		
	}
	
	@Override
	public boolean push(FixedHitReader e){
		if(e.read().getBundleKey() != null) {
			BytesRef bundleKey = e.read().getBundleKey();
			for (int i = 1; i <= size; i++) {
				if(bundleKey.equals(((HitElement) ((FixedHitReader) heap[i]).read()).getBundleKey())){
					//동일 bundle 이 존재하면 또다시 push하지 않는다.
					logger.debug("동일 Bundle found!!!");
					return false;
				}
			}
		}
		//bundle을 사용하지 않거나 동일 bundle이 없으면 push한다. 
		return super.push(e);	
	}
	
	@Override
	protected int compareTo(FixedHitReader r1, FixedHitReader r2) {
		
		HitElement one = r1.read();
		HitElement two = r2.read();
		
		for (int i = 0; i < sortFunctions.length; i++) {
			//하나씩 비교해가면서 각 funtion의 비교결과가 0이 아닐때 까지 비교한다.
			int r = sortFunctions[i].compare(one.rankData(i), two.rankData(i));
			if(r != 0){
				return r;
			}
		}
		
		if(r1.collectionId() == r2.collectionId()){
			//정렬 데이터가 모두 같다면 문서번호가 최신인걸 보여준다. 
//			return two.docNo() - one.docNo();
			return one.compareTo(two);
		}else{
			//컬렉션이 shard가 다르면 비교하지 않고 같음으로 넘긴다.
			return 0;
		}
	}

}
