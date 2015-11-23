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

import java.io.PrintStream;


public class OrOperatedClause extends OperatedClause {
	private OperatedClause clause1;
	private OperatedClause clause2;
	private boolean hasNext1 = true;
	private boolean hasNext2 = true;
	
	private RankInfo docInfo1;
	private RankInfo docInfo2;
    private int proximity;
	
	public OrOperatedClause(OperatedClause clause1, OperatedClause clause2) {
		super("OR");
		this.clause1 = clause1;
		this.clause2 = clause2;
	}

    public OrOperatedClause(OperatedClause clause1, OperatedClause clause2, int proximity) {
        this(clause1, clause2);
        this.proximity = proximity;
    }
	
	protected boolean nextDoc(RankInfo rankInfo) {
		while(hasNext1 || hasNext2){
			int doc1 = docInfo1.docNo();
			int doc2 = docInfo2.docNo();
			if(hasNext1 && hasNext2){
				if(doc1 < doc2){
					rankInfo.init(doc1, docInfo1.score(), docInfo1.hit());
					rankInfo.addMatchFlag(docInfo1.matchFlag());
					rankInfo.explain(docInfo1);
					hasNext1 = clause1.next(docInfo1);
				}else if(doc1 > doc2){
					rankInfo.init(doc2, docInfo2.score(), docInfo2.hit(), docInfo2.positions());
					rankInfo.addMatchFlag(docInfo2.matchFlag());
					rankInfo.explain(docInfo2);
					hasNext2 = clause2.next(docInfo2);
				}else {
                    int score = docInfo1.score() + docInfo2.score();
                    if (proximity != 0) {
                        int[] pos1 = docInfo1.positions();
                        int[] pos2 = docInfo2.positions();
                        if (pos1 != null && pos2 != null) {
                            OUTER:
                            for (int p1 : pos1) {
                                for (int p2 : pos2) {
//                                    logger.debug("{}>>{}:{}", doc1, p1, p2);
                                    if (proximity > 0) {
                                        //순서존재.
                                        int diff = p2 - p1;
                                        if (diff == 1) {
                                            //인접확인.
                                            score *= 20; //정확히 1차이.
                                            break OUTER;
                                        } else if (diff >= 0 && diff <= proximity) {
                                            score *= 10; //1이 아닌 0이나 2이상.
                                            break OUTER;
                                        }
                                    } else {
                                        //순서없음.
                                        int diff = p2 - p1;
                                        if(diff > 0) {
                                            if(diff <= -proximity) {
                                                score *= 10;
                                                break OUTER;
                                            }
                                        } else if(diff <= 0) {
                                            if(diff >= proximity) {
                                                score *= 10; //순서바뀜.
                                                break OUTER;
                                            }
                                        }
//                                        if (diff >= proximity && diff <= -proximity) {
//                                            //인접확인.
//                                            score *= 50;
//                                            break OUTER;
//                                        }
                                    }
                                }
                            }
                        }
                    }
                    rankInfo.init(doc1, score, docInfo1.hit() + docInfo2.hit(), docInfo2.positions());
                    rankInfo.addMatchFlag(docInfo1.matchFlag());
                    rankInfo.addMatchFlag(docInfo2.matchFlag());
                    rankInfo.explain(docInfo1);
                    rankInfo.explain(docInfo2);
                    hasNext1 = clause1.next(docInfo1);
                    hasNext2 = clause2.next(docInfo2);
                }

                return true;
			}

			if(hasNext1){
				rankInfo.init(doc1, docInfo1.score(), docInfo1.hit());
				rankInfo.addMatchFlag(docInfo1.matchFlag());
				rankInfo.explain(docInfo1);
				hasNext1 = clause1.next(docInfo1);
				return true;
			}
		
			if(hasNext2){
				rankInfo.init(doc2, docInfo2.score(), docInfo2.hit(), docInfo2.positions());
				rankInfo.addMatchFlag(docInfo2.matchFlag());
				rankInfo.explain(docInfo2);
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

	@Override
	protected void initClause(boolean explain) {
		docInfo1 = new RankInfo(explain);
		docInfo2 = new RankInfo(explain);

        if(clause1 != null) {
            clause1.init(explanation != null ? explanation.createSubExplanation() : null);
        }
        if(clause2 != null) {
            clause2.init(explanation != null ? explanation.createSubExplanation() : null);
        }
        if(clause1 != null) {
            hasNext1 = clause1.next(docInfo1);
        } else {
            hasNext1 = false;
        }
        if(clause2 != null) {
            hasNext2 = clause2.next(docInfo2);
        } else {
            hasNext2 = false;
        }
	}
	
	@Override
	public OperatedClause[] children() {
		return new OperatedClause[] {
			clause1,
			clause2
		};
	}

	@Override
	public void printTrace(PrintStream os, int depth) {
		int indentSize = 4;
		String indent = "";
		if(depth > 0){
			for (int i = 0; i < (depth - 1) * indentSize; i++) {
				indent += " ";
			}
			
			for (int i = (depth - 1) * indentSize, p = 0; i < depth * indentSize; i++, p++) {
				if(p == 0){
					indent += "|";
				}else{
					indent += "-";
				}
			}
		}
		os.println(indent+"[OR]");
		if(clause1 != null){
			clause1.printTrace(os, depth + 1);
		}
		if(clause2 != null){
			clause2.printTrace(os, depth + 1);
		}
	}
}
