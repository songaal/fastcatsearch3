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


@TokenizerAttributes ( name="Whitespace" )
public class WhitespaceTokenizer extends FieldTokenizer{

//	private char[] SPACE_CHARACTER = {' ','\t','\n','\r',',','/','.'};
	private char[] SPACE_CHARACTER = {' ','\t','\n','\r'};
	private int pos;
	private int limit;
	public int seq = -1;
	
	public WhitespaceTokenizer() { }
	
	public boolean nextToken(CharVector token) {
		
		//처음 공백을 무시한다.
		while(pos < limit && 
				(input.array[pos] == SPACE_CHARACTER[0] || input.array[pos] == SPACE_CHARACTER[1]
			 || input.array[pos] == SPACE_CHARACTER[2] || input.array[pos] == SPACE_CHARACTER[3]
			)){
			pos++;
		}
		
		if(pos < limit){
			int start = pos;
			while(pos < limit && input.array[pos] != SPACE_CHARACTER[0] && input.array[pos] != SPACE_CHARACTER[1]
			      && input.array[pos] != SPACE_CHARACTER[2] && input.array[pos] != SPACE_CHARACTER[3]
                  ){
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

	protected void init() {
		pos = input.start;
		limit = pos + input.length;		
		seq = -1;
	}

}
