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

@TokenizerAttributes ( name="NaiveTerm" )
public class NaiveTermTokenizer extends Tokenizer{

	private char[] SPACE_CHARACTER = {' ','\t','\n','\r'};
	private int pos;
	private int limit;
	
	private int type = -1;
	private int oldType = 0;
	private char prevChar = 0;
	
	public NaiveTermTokenizer() { }
	
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
			int lastIgnore = 0; //마지막에 출현하면 무시하는 문자들.
			while(pos < limit){
				
				if(input.array[pos] == SPACE_CHARACTER[0] || input.array[pos] == SPACE_CHARACTER[1]
				      || input.array[pos] == SPACE_CHARACTER[2] || input.array[pos] == SPACE_CHARACTER[3]){
					type = 0;
				//아래는 허용된 특수문자	
				}else if((input.array[pos] >= 'a' && input.array[pos] <=  'z') 
						|| (input.array[pos] >= 'A' && input.array[pos] <=  'Z')
						|| (input.array[pos] >= '0' && input.array[pos] <=  '9')
						|| input.array[pos] == '%' || input.array[pos] == '*'
						){
					type = 1;
					lastIgnore = 0;
				}else if(
						//영문숫자와 붙어서 출현할수 있는 문자들.
						input.array[pos] == '$' || input.array[pos] == '_'
						|| input.array[pos] == '@' || input.array[pos] == '.'
						){
					type = 1;
					lastIgnore++;
				}else if(prevChar >= '0' && prevChar <=  '9' && input.array[pos] == ','){
					//숫자안에 컴마는 유효하다.
					type = 1;
					lastIgnore++; //하지만 200, 과 같이 컴마로 끝나는 경우는 유효하지 않다. 
					
				}else if(input.array[pos] == '-' || input.array[pos] == '&'
						//영문숫자와는 구별해야하는 문자들.
							|| input.array[pos] == ','
							){
						type = 2;
				}else if(input.array[pos] >= '가' && input.array[pos] <=  '힣'){
					type = 4;
				}else{
					type = -1;
				}
				
				if(oldType == 0 && type != 0){
					seq++;
				}
				
//				logger.debug(input.array[pos]+" => "+type+", seq="+seq);
				
				prevChar = input.array[pos];
				
				if(type != oldType){
					
					if(oldType != -1 && oldType != 0){
						oldType = type;
						
						int len  = pos - start - lastIgnore;
						if(len <= 1){
							//한글자 이하는 무시한다.
							start = pos;
						}else{
							break;
						}
						
					}
				}
				
				if(type == -1 || type == 0){
					start++;
				}
				
				pos++;
				
				oldType = type;
				
			}
			
			
			int len = pos - start - lastIgnore;
//			logger.debug("len = "+len);
			if(len > 0){
				token.array = input.array;
				token.start = start;
				token.length = len;
				token.hash = 0;
				
				return true;
//				logger.debug("{} >> lastIgnore={}", token, lastIgnore);
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
