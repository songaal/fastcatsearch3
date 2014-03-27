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


public class OrOperatedClause implements OperatedClause {
	private OperatedClause clause1;
	private OperatedClause clause2;
	private boolean hasNext1 = true;
	private boolean hasNext2 = true;
	
	private RankInfo docInfo1 = new RankInfo();
	private RankInfo docInfo2 = new RankInfo();
	
	public OrOperatedClause(OperatedClause clause1, OperatedClause clause2) {
		this.clause1 = clause1;
		this.clause2 = clause2;
		
		hasNext1 = clause1.next(docInfo1);
		hasNext2 = clause2.next(docInfo2);
	}

	public boolean next(RankInfo docInfo) {
		
		if(hasNext1 || hasNext2){
			int doc1 = docInfo1.docNo();
			int doc2 = docInfo2.docNo();
			
			if(hasNext1 && hasNext2){
				if(doc1 < doc2){
					docInfo.init(doc1, docInfo1.score(), docInfo1.hit());
					hasNext1 = clause1.next(docInfo1);
				}else if(doc1 > doc2){
					docInfo.init(doc2, docInfo2.score(), docInfo2.hit());
					hasNext2 = clause2.next(docInfo2);
				}else{
					docInfo.init(doc1, docInfo1.score() + docInfo2.score(), docInfo1.hit() + docInfo2.hit());
					hasNext1 = clause1.next(docInfo1);
					hasNext2 = clause2.next(docInfo2);
				}
				return true;
			}
			
			if(hasNext1){
				docInfo.init(doc1, docInfo1.score(), docInfo1.hit());
				hasNext1 = clause1.next(docInfo1);
				return true;
			}
		
			if(hasNext2){
				docInfo.init(doc2, docInfo2.score(), docInfo2.hit());
				hasNext2 = clause2.next(docInfo2);
				return true;
			}
			
		}
		
		return false;
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

}
