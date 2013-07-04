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
import org.fastcatsearch.ir.search.posting.TermDocsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeTermDoc {
	private Logger logger = LoggerFactory.getLogger(CompositeTermDoc.class);
	private int indexFieldNum;
	private CharVector term;
	private TermDoc[] termDocList;
	private int count;

	public CompositeTermDoc(int indexFieldNum, CharVector term, int size) {
		this(indexFieldNum, term, new TermDoc[size], 0);
	}

	public CompositeTermDoc(int indexFieldNum, CharVector term, TermDoc[] termDocList, int count) {
		this.indexFieldNum = indexFieldNum;
		this.termDocList = termDocList;
		this.term = term;
		this.count = count;
	}

	public int indexFieldNum() {
		return indexFieldNum;
	}

	public CharVector term() {
		return term;
	}

	public int count() {
		return count;
	}

	public TermDoc[] termDocList() {
		return termDocList;
	}

	public void setTermDocList(TermDoc[] termDocList) {
		this.termDocList = termDocList;
	}

	public int addTermDoc(TermDoc termDoc) {
		if (count == termDocList.length) {
			int newLength = (int) (termDocList.length * 1.2);
			try {
				TermDoc[] newTermDocList = new TermDoc[newLength];
				System.arraycopy(termDocList, 0, newTermDocList, 0, termDocList.length);
				termDocList = newTermDocList;
			} catch (OutOfMemoryError e) {
				logger.error("OOM! while allocating memory size = " + newLength, e);
				throw e;
			}
		}
		termDocList[count++] = termDoc;
		return count;
	}

	public TermDocsReader getReader(){
		return new TermDocsReader(this);
	}
}
