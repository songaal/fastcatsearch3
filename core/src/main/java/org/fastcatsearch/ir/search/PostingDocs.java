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

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.search.posting.PostingDocsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostingDocs {
	private static Logger logger = LoggerFactory.getLogger(PostingDocs.class);
//	private int indexFieldNum;
	private CharVector term;
	private PostingDoc[] postingDocList;
	private int count;

//	public PostingDocs(int indexFieldNum, CharVector term, int size) {
	public PostingDocs(CharVector term, int size) {
		this(term, new PostingDoc[size], 0);
	}

	public PostingDocs(CharVector term, PostingDoc[] postingDocList, int count) {
		this.postingDocList = postingDocList;
		this.term = term;
		this.count = count;
	}

//	public int indexFieldNum() {
//		return indexFieldNum;
//	}

	public CharVector term() {
		return term;
	}

	public int count() {
		return count;
	}

	public PostingDoc[] postingDocList() {
		return postingDocList;
	}

	public void setPostingDocList(PostingDoc[] postingDocList) {
		this.postingDocList = postingDocList;
	}

	public int addPostingDoc(PostingDoc termDoc) {
		if (count == postingDocList.length) {
			int newLength = (int) (postingDocList.length * 1.2);
			try {
				PostingDoc[] newPostingDocList = new PostingDoc[newLength];
				System.arraycopy(postingDocList, 0, newPostingDocList, 0, postingDocList.length);
				postingDocList = newPostingDocList;
			} catch (OutOfMemoryError e) {
				logger.error("OOM! while allocating memory size = " + newLength, e);
				throw e;
			}
		}
		postingDocList[count++] = termDoc;
		return count;
	}

	public PostingDocsReader getReader(){
		return new PostingDocsReader(this);
	}
}
