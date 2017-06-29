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

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.query.RowExplanation;

import java.util.List;

/**
 * Hit리스트를 구성하는 문서번호와 정렬정보 데이터  
 * @author sangwook.song
 *
 */
public class HitElement extends AbstractHitElement<HitElement> {

    protected float distance;
	protected int filterMatchOrder;
	//bundleKey는 타 노드로 전송할 필요없음. 즉, 한 컬렉션 내에서만 묶고, 컬렉션끼리의 병합시에는 묶지 않음. 
	private BytesRef bundleKey;
	
	private DocIdList bundleDocIdList;
	private int totalBundleSize;

	public HitElement(int docNo, int score, int hit, List<RowExplanation> list){
		this(null, docNo, score, hit, null, list);
	}
	public HitElement(int docNo, int score, int hit, BytesRef[] dataList, List<RowExplanation> list){
		this(null, docNo, score, hit, dataList, list);
	}
	//bundle
	public HitElement(int docNo, int score, int hit, BytesRef[] dataList, List<RowExplanation> list, BytesRef bundleKey){
		this(null, docNo, score, hit, dataList, list, bundleKey);
	}
	public HitElement(String segmentId, int docNo, int score, int hit, BytesRef[] dataList, List<RowExplanation> list){
		super(segmentId, docNo, score, hit, dataList, list);
	}
	public HitElement(String segmentId, int docNo, int score, int hit, BytesRef[] dataList, List<RowExplanation> list, DocIdList bundleDocIdList, int totalBundleSize){
		super(segmentId, docNo, score, hit, dataList, list);
		this.bundleDocIdList = bundleDocIdList;
        this.totalBundleSize = totalBundleSize;
	}

	//bundle
	public HitElement(String segmentId, int docNo, int score, int hit, BytesRef[] dataList, List<RowExplanation> list, BytesRef bundleKey){
		super(segmentId, docNo, score, hit, dataList, list);
		this.bundleKey = bundleKey;
	}

    public float distance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

	public int filterMatchOrder() {
		return filterMatchOrder;
	}

	public void setFilterMatchOrder(int filterMatchOrder) {
		this.filterMatchOrder = filterMatchOrder;
	}

	@Override
	public String toString(){
		if(bundleKey != null) {
			return super.toString() + ":" + bundleKey + ":" + totalBundleSize;
		} else {
			return super.toString();
		}
	}
	
	@Override
	public int compareTo(HitElement other) {
		//최신세그먼트 우선.
		if(segmentId != null && other.segmentId != null && !segmentId.equals(other.segmentId)){
			return segmentId.compareTo(other.segmentId);
		}
		
		//정렬 데이터가 모두 같다면 문서번호가 최신인걸 보여준다. 
		return other.docNo - docNo;
	}
	
	public void setBundleKey(BytesRef bundleKey) {
		this.bundleKey = bundleKey;
	}

	public BytesRef getBundleKey() {
		return bundleKey;
	}
	
	public void setBundleDocIdList(DocIdList bundleDocIdList) {
		this.bundleDocIdList = bundleDocIdList;
	}
	
	public DocIdList getBundleDocIdList() {
		return bundleDocIdList;
	}

    public void setTotalBundleSize(int totalBundleSize) {
        this.totalBundleSize = totalBundleSize;
    }

    public int getTotalBundleSize() {
        return totalBundleSize;
    }
}
