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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 조건없이 모든 문서를 리턴한다.
 * @author sangwook.song
 *
 */
public class AllDocumentOperatedClause implements OperatedClause {
	private static Logger logger = LoggerFactory.getLogger(AllDocumentOperatedClause.class);
	private int docCount;
	private int pos;
	
	public AllDocumentOperatedClause(int docCount) {
		this.docCount = docCount;
	}

	public boolean next(RankInfo docInfo) {
		if(pos < docCount){
			docInfo.init(pos, 0);
//			logger.debug(">>"+ docInfo.docNo());
			pos++;
			return true;
		}
		docInfo.init(-1,-1);
		return false;
	}

}
