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

public class AndOperatedClause extends OperatedClause {
	private OperatedClause clause1;
	private OperatedClause clause2;
	private boolean hasNext1 = true;
	private boolean hasNext2 = true;
	private RankInfo docInfo1;
	private RankInfo docInfo2;
	
	public AndOperatedClause(OperatedClause clause1, OperatedClause clause2) {
		super("AND");
		this.clause1 = clause1;
		this.clause2 = clause2;
	}

	protected boolean nextDoc(RankInfo rankInfo) {
		hasNext1 = clause1.next(docInfo1);
		hasNext2 = clause2.next(docInfo2);

		if(hasNext1 && hasNext2){
			int doc1 = docInfo1.docNo();
			int doc2 = docInfo2.docNo();
			while(hasNext1 && hasNext2 && (doc1 != doc2)){
				while(hasNext1 && (doc1 < doc2)){
					hasNext1 = clause1.next(docInfo1);
					doc1 = docInfo1.docNo();
				}
				while(hasNext2 && (doc1 > doc2)){
					hasNext2 = clause2.next(docInfo2);
					doc2 = docInfo2.docNo();
				}
			}
			
			if(hasNext1 && hasNext2 && (doc1 == doc2)){
				rankInfo.explain(docInfo1);
				rankInfo.explain(docInfo2);
				rankInfo.init(doc1, docInfo1.score() + docInfo2.score(), docInfo1.hit() + docInfo2.hit());
				return true; 
			}
			
			return false;
		}
		
		//절1과 절2중 하나라도 끝나면 AND 집합도 더이상 없는것이다.
		return false;
	}
	
	@Override
	public String toString(){
		return "["+getClass().getSimpleName()+"]"
				+ (clause1!= null?clause1.toString():"null") + " / "
						+ (clause2!= null?clause2.toString():"null");
	}

	@Override
	public void close() {
		if(clause1 != null){
			clause1.close();
		}
		if(clause2 != null){
			clause2.close();
		}
	}

	@Override
	protected void initClause(boolean explain) {
		docInfo1 = new RankInfo(explain);
		docInfo2 = new RankInfo(explain);
		clause1.init(explanation != null ? explanation.createSubExplanation() : null);
		clause2.init(explanation != null ? explanation.createSubExplanation() : null);
	}

//	@Override
//	protected void initExplanation() {
//		if(clause1 != null) {
//			clause1.setExplanation(explanation.createSub1());
//		}
//		if(clause2 != null) {
//			clause2.setExplanation(explanation.createSub2());
//		}
//	}

}
