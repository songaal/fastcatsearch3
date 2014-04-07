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
import org.fastcatsearch.ir.search.ClauseExplanation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class OperatedClause {
	protected static Logger logger = LoggerFactory.getLogger(OperatedClause.class);
	
	protected String id;
	protected ClauseExplanation explanation;
	private boolean isReady;
	
	public OperatedClause(String id){
		this.id = id;
	}
	
	// 사용하기전에 호출한다.
	public void init() {
		init(null);
	}
	
	public void init(ClauseExplanation explanation) {
		if(isReady){
			return;
		}
		if(explanation != null){
			setExplanation(explanation);
		}
		
		initClause();
		isReady = true;
	}
	
	protected abstract void initClause();

	public void setExplanation(ClauseExplanation explanation) {
		this.explanation = explanation;
		if(explanation != null) {
			explanation.setId(id);
		}
		initExplanation();
	}
	
	protected abstract void initExplanation();
	
	/**
	 * @param docInfo
	 * @return RankInfo를 올바로 읽었는지 여부. 
	 */
	public boolean next(RankInfo docInfo) {
		if(explanation != null){
			long start = System.nanoTime();
			if(nextDoc(docInfo)){
				explanation.set(docInfo.score(), docInfo.hit());
				explanation.addTime(System.nanoTime() - start);
				explanation.addRow();
				return true;
			}else{
				explanation.set(0, 0);
				explanation.addTime(System.nanoTime() - start);
				return false;
			}
		}else{
			return nextDoc(docInfo);
		}
	}
	
	protected abstract boolean nextDoc(RankInfo docInfo);
	
	public abstract void close();
	
	public String id(){
		return id;
	}
	
	public String term() {
		return null;
	}
}
