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

package org.fastcatsearch.ir.analysis;

import org.fastcatsearch.ir.io.CharVector;

@TokenizerAttributes ( name="Tabbed" )
public class TabedTokenizer extends FieldTokenizer{

	private char DELIMETER = '\t';
	private int pos;
	private int limit;
	public int seq = -1;
	
	public TabedTokenizer() { }
	
	public boolean nextToken(CharVector token) {
		
		while(pos < limit && input.array[pos] == DELIMETER){
			pos++;
		}
		
		if(pos < limit){
			int start = pos;
			while(pos < limit && input.array[pos] != DELIMETER){
				pos++;
			}
			int len = pos - start;
			token.array = input.array;
			token.start = start;
			token.length = len;
			token.hash = 0;
			seq++;
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected void init() {
		pos = input.start;
		limit = pos + input.length;	
		seq = -1;
	}

}
