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


/**
 * 외부에서 직접 문서번호정보를 넣어 절을 만든다.
 * @author sangwook.song
 *
 */
public class UserOperatedClause implements OperatedClause {
	
	private int pos;
	private int count;
	private int[] docs;
	private float[] weight;
	
	public UserOperatedClause(int count, int[] docs, float[] weight) {
		this.count = count;
		this.docs = docs;
		this.weight = weight;
	}

	public boolean next(RankInfo docInfo) {
		if(pos < count){
			docInfo.init(docs[pos], weight[pos]);
			pos++;
			return true;
		}
		docInfo.init(-1,-1);
		return false;
	}

	@Override
	public void close() {
	}

}
