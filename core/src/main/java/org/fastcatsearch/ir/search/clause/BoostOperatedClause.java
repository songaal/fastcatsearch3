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

package org.fastcatsearch.ir.search.clause;

import org.fastcatsearch.ir.query.RankInfo;


/**
 * mainClause 를 기준으로 boostClause 에 일치하는 문서가 있다면 점수를 올려준다.
 * 결과 set은 mainClause의 것이 그대로 유지되며, boostClause는 점수에만 영향을 준다.
 * 
 * */
public class BoostOperatedClause implements OperatedClause {
	private OperatedClause mainClause;
	private OperatedClause boostClause;
	private RankInfo docInfo1 = new RankInfo();
	private RankInfo docInfo2 = new RankInfo();
	
	public BoostOperatedClause(OperatedClause mainClause, OperatedClause boostClause) {
		this.mainClause = mainClause;
		this.boostClause = boostClause;
	}

	public boolean next(RankInfo docInfo) {
		
		float newScore = 0;
		while(mainClause.next(docInfo1)){
			int doc1 = docInfo1.docNo();
			newScore = docInfo1.score();
			while(boostClause.next(docInfo2)){
				if(doc1 == docInfo2.docNo()){
					newScore += docInfo2.score();
					break;
				}else if(doc1 < docInfo2.docNo()){
					break;
				}
			}
			
			docInfo.init(doc1, newScore);
			return true; 
			
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + "]" + (mainClause != null ? mainClause.toString() : "null") + " / " + (boostClause != null ? boostClause.toString() : "null");
	}

	@Override
	public void close() {
		if(mainClause != null){
			mainClause.close();
		}
		if(boostClause != null){
			boostClause.close();
		}
	}

}
