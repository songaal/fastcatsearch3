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

@TokenizerAttributes ( name="Simple" )
public class TypeTokenizer extends Tokenizer{

	protected char[] SPACE_CHARACTER = {' ','\t','\n','\r'};
	protected int pos;
	protected int limit;
	
	protected int type = -1;
	protected int oldType = 0;
	
	public TypeTokenizer() { }
	
	public int seq = -1;
	
	public boolean nextToken(CharVector token,int[] seq) {
		boolean b = nextToken(token);
		seq[0] = this.seq;
		return b;
	}
	
	public boolean nextToken(CharVector token) {
		
		//처음 공백을 무시한다.
		while(pos < limit && 
				(input.array[pos] == SPACE_CHARACTER[0] || input.array[pos] == SPACE_CHARACTER[1]
			 || input.array[pos] == SPACE_CHARACTER[2] || input.array[pos] == SPACE_CHARACTER[3])){
			pos++;
		}
		
		if(pos < limit){
//			int type = -1;
			int start = pos;
			//한문자씩 타입을 확인한다.
			while(pos < limit){
				
				
				if(input.array[pos] == SPACE_CHARACTER[0] || input.array[pos] == SPACE_CHARACTER[1]
				      || input.array[pos] == SPACE_CHARACTER[2] || input.array[pos] == SPACE_CHARACTER[3]){
					type = 0;
				//TODO:별도의 특수문자 허용 요청이 들어올 경우 아래 에 나열할 것	
				}else if((input.array[pos] >= 'a' && input.array[pos] <=  'z') 
						|| (input.array[pos] >= 'A' && input.array[pos] <=  'Z')
						|| input.array[pos] == '-' || input.array[pos] == '&' || input.array[pos] == '/'
						|| input.array[pos] == '$' || input.array[pos] == '%' //|| input.array[pos] == '_'
						|| input.array[pos] == '@'
						||(input.array[pos] >= '0' && input.array[pos] <=  '9')){
					type = 2;
				}else if(input.array[pos] >= 'ㄱ' && input.array[pos] <=  '힣'){
					type = 3;
				}else{
					type = -1;
				}
				
				if(oldType == 0 && type != 0){
					seq++;
				}
				
//				logger.debug(input.array[pos]+" => "+type+", seq="+seq);
				if(type != oldType){
					
					if(oldType != -1 && oldType != 0){
						oldType = type;
						break;
					}
				}
				
				if(type == -1 || type == 0){
					start++;
//					logger.debug("Start++");
				}
				
				pos++;
				
				oldType = type;
			}
			
			
			int len = pos - start;
//			logger.debug("len = "+len);
			if(len > 0){
				token.array = input.array;
				token.start = start;
				token.length = len;
				token.hash = 0;
				return true;
			}else{
				return false;
			}
			
		}else{
			return false;
		}
	}

	protected void init() {
		pos = input.start;
		limit = pos + input.length;		
		seq = -1;
		oldType = 0;
	}

}
