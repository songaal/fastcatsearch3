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

import java.io.IOException;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.PostingReader;

public class TermOperatedClause extends OperatedClause {
	private static final int SCORE_BASE = 10000;
	private PostingReader postingReader;
	private int segmentDF;
	private int documentCount;

	private String termString;
	
	public TermOperatedClause(String indexId, PostingReader postingReader) throws IOException {
		super(indexId);
		if (postingReader != null) {
			this.postingReader = postingReader;
			this.segmentDF = postingReader.size();
			this.documentCount = postingReader.documentCount();
			termString = postingReader.term().toString();
		}
	}

	protected boolean nextDoc(RankInfo rankInfo) {
		if (postingReader == null) {
			rankInfo.init(-1, 0, 0);
			return false;
		}
		if (postingReader.hasNext()) {
			PostingDoc postingDoc = postingReader.next();
			int score = 0;
			if(postingReader.weight() > 0){
				float tf = 2.2f * postingDoc.tf() / (2.0f + postingDoc.tf());
				float idf = (float) Math.log(documentCount / segmentDF);
				score = (int) ((tf * idf * postingReader.weight()) * SCORE_BASE);
			}
			rankInfo.init(postingDoc.docNo(), score, postingDoc.tf());
			if(isExplain()){
				rankInfo.explain(id, score, postingReader.term().toString());
			}
			return true;
		} else {
			rankInfo.init(-1, 0, 0);
			return false;
		}
	}

	@Override
	public String toString() {
		if (postingReader != null) {
			return "[" + getClass().getSimpleName() + "]" + postingReader.term() + ":" + postingReader.size();
		} else {
			return "[" + getClass().getSimpleName() + "] null";
		}
	}

	@Override
	public void close() {
		if (postingReader == null) {
			postingReader.close();
		}
	}

	@Override
	protected void initClause(boolean explain) {
		if(explanation != null) {
			explanation.setTerm(postingReader.term().toString());
		}
	}

//	@Override
//	protected void initExplanation() {
//		explanation.setTerm(postingReader.term().toString());
//	}

	@Override
	public String term() {
		return termString;
	}
}
