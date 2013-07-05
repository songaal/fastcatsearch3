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


import java.io.IOException;

import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.CompositePostingDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class TermOperatedClause implements OperatedClause {
	private static Logger logger = LoggerFactory.getLogger(TermOperatedClause.class);
	private int pos;
	
	private CompositePostingDoc termDocs;
	private PostingDoc[] termDocList;
	
	private int weight;
	private boolean ignoreTermFreq;
	
	public TermOperatedClause(CompositePostingDoc termDocs, int weight) throws IOException {
		this(termDocs, weight, false);
	}
	public TermOperatedClause(CompositePostingDoc termDocs, int weight, boolean ignoreTermFreq) throws IOException {
		this.termDocs = termDocs;
		termDocList = termDocs.termDocList();
		this.weight = weight;
		this.ignoreTermFreq = ignoreTermFreq;
	}

	public CompositePostingDoc termDocs(){
		return termDocs;
	}
	/* (non-Javadoc)
	 * @see cat.ir.query.OperatedClause#next(cat.ir.query.RankInfo)
	 */
	public boolean next(RankInfo docInfo) {
		if(termDocs == null){
			docInfo.init(-1,-1);
			return false;
		}
		
		if(pos < termDocs.count()){
			PostingDoc termDoc = termDocList[pos];
			//if tf is greater than 1, score is weight + (tf - 1) * 1
			docInfo.init(termDoc.docNo(), ignoreTermFreq ? weight : weight + (termDoc.tf() - 1));
			//docInfo.init(docs[pos], tfs[pos] * weight);
			pos++;
			return true;
		}
		
		docInfo.init(-1,-1);
		return false;
	}
	
	@Override
	public String toString(){
		if(termDocs != null){
			return "["+getClass().getSimpleName()+"]"+termDocs.term()+":"+termDocs.count();
		}else{
			return "["+getClass().getSimpleName()+"] null";
		}
	}

}
