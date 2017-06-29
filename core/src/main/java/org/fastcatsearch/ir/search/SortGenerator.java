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

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.DistanceField;
import org.fastcatsearch.ir.field.HitField;
import org.fastcatsearch.ir.field.MatchOrderField;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.Sort;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * 각 쿼리당 개별적인 SortGenerator가 생성되어 소팅을 수행한다. 
 * getHitElement메소드에서 RankInfo를 받아서 sort필드를 참고하여, 최종적으로  HitElement를 리턴한다.
 * HitElement는 정렬시 필요한 데이터를 가지고 있으므로 각 세그먼트로 부터 모인 결과들을 재정렬할수 있게된다.
 * @author sangwook.song
 *
 */
public class SortGenerator {
	private Logger logger = LoggerFactory.getLogger(SortGenerator.class);
	private int[] fieldIndex; //쿼리 소트필드들의 필드번호
	private boolean[] isAscending; //쿼리 소트필드들의 정렬방식
//	private int dataSize; //쿼리 소트필드들의 데이터길이 
	private IndexRef<FieldIndexReader> indexRef;
	private BytesRef[] dataList;
	private int sortSize;//다중정렬갯수.
	
	public SortGenerator() throws IOException{
	}
	
	public SortGenerator(List<Sort> querySortList, Schema schema, FieldIndexesReader fieldIndexesReader) throws IOException{
//		this.dataSize = 0;
		if(querySortList != null && querySortList.size() > 0) {
			this.sortSize = querySortList.size();
            this.fieldIndex = new int[sortSize];
            this.isAscending = new boolean[sortSize];
            this.dataList = new BytesRef[sortSize];

            List<String> fieldIdList = new ArrayList<String>(sortSize);
            for (int i = 0; i < sortSize; i++) {
                Sort sort = querySortList.get(i);
                String fieldId = sort.fieldIndexId();

                int idx = schema.getFieldIndexSequence(fieldId);

                //save each sort field number in order
                if (idx == -1) {
                    if (fieldId.equalsIgnoreCase(ScoreField.fieldName)) {
                        fieldIndex[i] = ScoreField.fieldNumber;
//					dataSize += ScoreField.fieldSize;
                    } else if (fieldId.equalsIgnoreCase(HitField.fieldName)) {
                        fieldIndex[i] = HitField.fieldNumber;
//					dataSize += HitField.fieldSize;
                    } else if (fieldId.equalsIgnoreCase(DistanceField.fieldName)) {
                        fieldIndex[i] = DistanceField.fieldNumber;
                    } else if (fieldId.equalsIgnoreCase(MatchOrderField.fieldName)) {
                        fieldIndex[i] = MatchOrderField.fieldNumber;
                    } else {
                        throw new IOException("Unknown sort field name = " + fieldId);
                    }
                    fieldIdList.add(null);
                } else {
                    fieldIndex[i] = idx;
                    fieldIdList.add(fieldId);
//				dataSize += schema.getFieldSetting(fieldId).getByteSize();
                }
                isAscending[i] = sort.asc();
                logger.debug("##SortGenerator-{} => {}, isAscending[{}]={}", i, fieldId, isAscending[i]);

            }

            indexRef = fieldIndexesReader.selectIndexRef(fieldIdList.toArray(new String[0]));
            for (int sequence = 0; sequence < sortSize; sequence++) {
                //데이터와 연결되어 있는 필드만 추가해준다.
                if (fieldIndex[sequence] >= 0) {
                    dataList[sequence] = indexRef.getDataRef(sequence).bytesRef();
                }
                //score, hit 필드등은 여기서는 null이며, 아래 getHitElement 에서 읽을때 객체를 생성한다.
            }
        }
	}
	
	public HitElement[] getHitElement(RankInfo[] rankInfoList, int n) throws IOException{
		HitElement[] result = new HitElement[n];
		if(sortSize > 0) {
            for (int i = 0; i < n; i++) {
                RankInfo ri = rankInfoList[i];
                indexRef.read(ri.docNo());

                BytesRef[] rankData = readRankData(ri);
                result[i] = new HitElement(ri.docNo(), ri.score(), ri.hit(), rankData, rankInfoList[i].rowExplanations());
                result[i].setDistance(ri.distance());
                result[i].setFilterMatchOrder(ri.filterMatchOrder());
            }
        } else {
            for (int i = 0; i < n; i++) {
                RankInfo ri = rankInfoList[i];
                result[i] = new HitElement(ri.docNo(), ri.score(), ri.hit(), null, rankInfoList[i].rowExplanations());
                result[i].setDistance(ri.distance());
                result[i].setFilterMatchOrder(ri.filterMatchOrder());
            }
        }
		return result;
	}
	
	public void getHitElement(RankInfo[] rankInfoList, HitElement[] result, int n) throws IOException{

        if(sortSize > 0) {
            for (int i = 0; i < n; i++) {
                RankInfo ri = rankInfoList[i];
                indexRef.read(ri.docNo());

                BytesRef[] rankData = readRankData(ri);
                result[i] = new HitElement(ri.docNo(), ri.score(), ri.hit(), rankData, rankInfoList[i].rowExplanations());
                result[i].setDistance(ri.distance());
                result[i].setFilterMatchOrder(ri.filterMatchOrder());
            }
        } else {
            for (int i = 0; i < n; i++) {
                RankInfo ri = rankInfoList[i];
                result[i] = new HitElement(ri.docNo(), ri.score(), ri.hit(), null, rankInfoList[i].rowExplanations());
                result[i].setDistance(ri.distance());
                result[i].setFilterMatchOrder(ri.filterMatchOrder());
            }
        }
	}
	
	protected BytesRef[] readRankData(RankInfo ri) {
		BytesRef[] rankData = new BytesRef[sortSize];
		for (int j = 0; j < sortSize; j++) {
			//정렬은 멀티밸류를 지원하지 않으며, 싱글밸류이기 때문에 즉시 bytesRef로 읽도록 한다.
			if(fieldIndex[j] == ScoreField.fieldNumber){
				rankData[j] = new BytesRef(ScoreField.fieldSize);
				IOUtil.writeInt(rankData[j], Float.floatToIntBits(ri.score()));
				rankData[j].flip();
			}else if(fieldIndex[j] == HitField.fieldNumber){
				rankData[j] = new BytesRef(HitField.fieldSize);
				IOUtil.writeInt(rankData[j], ri.hit());
				rankData[j].flip();
            }else if(fieldIndex[j] == DistanceField.fieldNumber){
                rankData[j] = new BytesRef(DistanceField.fieldSize);
                IOUtil.writeInt(rankData[j], Float.floatToIntBits(ri.distance()));
                rankData[j].flip();
			}else if(fieldIndex[j] == MatchOrderField.fieldNumber) {
                rankData[j] = new BytesRef(MatchOrderField.fieldSize);
                IOUtil.writeInt(rankData[j], ri.filterMatchOrder());
                rankData[j].flip();
            } else {
//				BytesRef bytesRef = indexRef.getDataRef(j).bytesRef();
//				rankData[j] = bytesRef.duplicate();
				rankData[j] = dataList[j].duplicate();
			}
		}
		
		return rankData;
	}
}























