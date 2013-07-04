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

package org.fastcatsearch.ir.query;

import java.util.List;

import org.fastcatsearch.ir.search.CollectedEntry;
import org.fastcatsearch.ir.search.TermDoc;
import org.fastcatsearch.ir.search.TermDocCollector;
import org.fastcatsearch.ir.search.TermDocTreeReader;
import org.fastcatsearch.ir.search.CompositeTermDoc;
import org.fastcatsearch.ir.search.posting.TermDocsMerger;
import org.fastcatsearch.ir.search.posting.TermDocsTreeNode;


public class MultiTermOperatedClause implements OperatedClause {

	private TermDocTreeReader termDocTreeReader;
	private int termCount;
//	private List<TermDoc> totalTermDocList;
	private TermDocCollector termDocCollector;
	private boolean storePosition;
	
	public MultiTermOperatedClause() {
		this(false);
	}

	public MultiTermOperatedClause(boolean storePosition) {
		this.storePosition = storePosition;
		termDocTreeReader = new TermDocTreeReader();
	}
	
	public void addTerm(CompositeTermDoc termDocs, int queryPosition) {
		addTerm(termDocs, queryPosition);
	}
	
	public void addTerm(CompositeTermDoc termDocs, int queryPosition, List<CompositeTermDoc> synonymList) {
		
		if(termDocs != null){
			termDocTreeReader.addNode(new TermDocsTreeNode(termDocs, queryPosition));
			termCount++;
		}
		
		CompositeTermDoc sysnonymTermDocs = null;
		if(synonymList != null){
			if(synonymList.size() == 1){
				sysnonymTermDocs = synonymList.get(0);
			}else{
				sysnonymTermDocs = new TermDocsMerger(synonymList).merge(termDocs.indexFieldNum(), termDocs.term(), 1024);
			}
			termDocTreeReader.addNode(new TermDocsTreeNode(sysnonymTermDocs, queryPosition, true));
			termCount++;
		}
		
		
	}

	
	public boolean next(RankInfo docInfo) {
		if(termDocCollector == null){
			termDocCollector = new TermDocCollector(termCount);		
		}else{
			termDocCollector.clear();
		}
		
		
		//동일문서번호에 대한 TermDoc list를 리턴받는다.
		int docNo = termDocTreeReader.next(termDocCollector);
				
		if(docNo < 0){
			return false;
		}else{
//			logger.debug(">> phrase doc={}, term size={}", docNo, totalTermDocList.size());
			float score = 0f;
			//쿼리와 비교한 여러단어에 대한 점수계산.
			if(storePosition){
				float proximityScore = 0f;
				float documentScore = 0f;
				float sumOfTPI = 0f;
				for (int i = 0; i < termDocCollector.size(); i++) {
					CollectedEntry entry = termDocCollector.get(i);
					TermDoc termDoc = entry.termDoc();
					//okapi점수에 tf만 사용. 쿼리점수무시. idf무시.
					documentScore += (2.2f * (float)termDoc.tf() / (2.0f + (float)termDoc.tf()));
					//score += 1000; //tf 는 무시. 한단어가 여러번 나오는 것은 그다지 중요하지 않음.
					for (int j = i + 1; j < termDocCollector.size(); j++) {
						
						//TODO doc1과 doc2가 유사어관계면 점수계산하지 않는다. 근데 누구의 유사어인지 확인할 방법이없네..
						//TODO 같은 단어를 두번이상 쿼리에 입려했을 경우 무시하는 로직 필요.?
						
						CollectedEntry entry2 = termDocCollector.get(j);
						TermDoc termDoc2 = entry.termDoc();

						int origianlPositionGap = entry2.queryPosition() - entry.queryPosition();
						
						int[] positions = termDoc.positions();
						int[] positions2 = termDoc2.positions();
//						logger.debug("pos1= {}", positions);
//						logger.debug("pos2= {}", positions2);
						for (int i2 = 0; i2 < positions.length; i2++) {
							for (int j2 = 0; j2 < positions2.length; j2++) {
								
								int actualPositionGap = positions2[j2] - positions[i2];
								
								int positionGapDiff =  origianlPositionGap - actualPositionGap;
								
								//d(ti, tj) = 원래차이 - 실제차이 => 원래차이와 실제차이가 같을수록 즉 0에 가까울수록 tpi가 높은 점수가 된다.
								//tpi(ti,tj) = 1.0 / d(t1, tj)^2
								//완벽한 TPRSV 점수를구하려면 문서길이/문서평균길이를 이용한 K 값을 계산해야 하나 여기서는 생략한다. 
								float tpi = (float) (1.0 / (1.0 + Math.pow(positionGapDiff, 2.0)));
								sumOfTPI += tpi;
//								logger.debug("{}: {} = {}", positions[i2], positions2[j2], tpi);
							}
						}
					}
				}
				//Okapi점수를 계산하여 tpi점수와 더해야 최종 점수가 계산됨.
				proximityScore = 2.2f * sumOfTPI / (2.0f + sumOfTPI);
				
				score = documentScore + proximityScore;
//				logger.debug(">> {} = {}+{}", score, documentScore, proximityScore);
			}else{
				//위치정보가 없으면 tf를 점수로 만든다.
				//Okapi 점수 계산
				for (int i = 0; i < termDocCollector.size(); i++) {
					TermDoc termDoc = termDocCollector.get(i).termDoc();
					score += (2.2f * (float)termDoc.tf() / (2.0f + (float)termDoc.tf()));
				}
			}
			docInfo.init(docNo, score);
		}
		
		//TODO점수가 일정치이하이면 버린다.
		
		return true;
	}
	
}
