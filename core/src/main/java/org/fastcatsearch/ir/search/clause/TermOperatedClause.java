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



public class TermOperatedClause implements OperatedClause {
	private PostingReader postingReader;
	private boolean ignoreTermFreq;
	
	public TermOperatedClause(PostingReader postingReader) throws IOException {
		this(postingReader, false);
	}
	public TermOperatedClause(PostingReader postingReader, boolean ignoreTermFreq) throws IOException {
		this.postingReader = postingReader;
		this.ignoreTermFreq = ignoreTermFreq;
	}

	public boolean next(RankInfo docInfo) {
		if(postingReader == null){
			return false;
		}
		if(postingReader.hasNext()){
			PostingDoc postingDoc = postingReader.next();
			docInfo.init(postingDoc.docNo(), ignoreTermFreq ? postingReader.weight() : postingReader.weight() + (postingDoc.tf() - 1));
			return true;
		}else{
			docInfo.init(-1,-1);
			return false;
		}
	}
	
	@Override
	public String toString(){
		if(postingReader != null){
			return "["+getClass().getSimpleName()+"]"+postingReader.term()+":"+postingReader.size();
		}else{
			return "["+getClass().getSimpleName()+"] null";
		}
	}
	@Override
	public void close() {
		if(postingReader == null){
			postingReader.close();
		}
	}

}
