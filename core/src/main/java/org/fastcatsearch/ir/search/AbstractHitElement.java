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

import java.util.List;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.query.RowExplanation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hit리스트를 구성하는 문서번호와 정렬정보 데이터  
 * @author sangwook.song
 *
 */
public abstract class AbstractHitElement<T> implements Comparable<T> {
	protected static Logger logger = LoggerFactory.getLogger(AbstractHitElement.class);
	
	protected String collectionId; //transient. ShardSearchResult에서 정보를 가지고 있음. 
	protected int segmentSequence;
	protected int docNo;
	protected int score; //매칭점수
	protected BytesRef[] rankData; //필드값으로 정렬할 경우 필드값 데이터
	protected List<RowExplanation> list;
	
//	public AbstractHitElement(int docNo, int score, List<RowExplanation> list){
//		this(-1, docNo, score, null, list);
//	}
//	public AbstractHitElement(int docNo, int score, BytesRef[] dataList, List<RowExplanation> list){
//		this(-1, docNo, score, dataList, list);
//	}
	public AbstractHitElement(int segmentSequence, int docNo, int score, BytesRef[] dataList, List<RowExplanation> list){
		this.segmentSequence = segmentSequence;
		this.docNo = docNo;
		this.score = score;
		this.rankData = dataList;
		this.list = list;
	}
	public String collectionId(){
		return collectionId;
	}
	public void setCollectionId(String collectionId){
		this.collectionId = collectionId;
	}
	public int segmentSequence(){
		return segmentSequence;
	}
	public int docNo(){
		return docNo;
	}
	public void setSegmentSequence(int sequence){
		segmentSequence = sequence;
	}
	
	public void setDocNo(int segmentSequence, int docNo){
		this.segmentSequence = segmentSequence;
		this.docNo = docNo;
	}
	public int score(){
		return score;
	}
	
	public BytesRef[] rankData(){
		return rankData;
	}
	
	public BytesRef rankData(int i){
		return rankData[i];
	}
	
	public int rankDataSize(){
		if(rankData == null){
			return 0;
		}
		return rankData.length;
	}
	
	public List<RowExplanation> rowExplanations(){
		return list;
	}
	
//	public HitElement addBaseDocNo(int baseDocNo){
//		docNo += baseDocNo;
//		return this;
//	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[seg#");
		sb.append(segmentSequence);
		sb.append("]");
		sb.append(docNo);
		sb.append(":");
		sb.append(score);
		sb.append(":");
//		if(rankdata != null){
//			for (int i = 0; i < dataLen; i++) {
//				sb.append(rankdata[dataOffset + i]);
//				if(i < dataLen - 1)
//					sb.append(",");
//			}
//		}
		return sb.toString();
	}
	
}
