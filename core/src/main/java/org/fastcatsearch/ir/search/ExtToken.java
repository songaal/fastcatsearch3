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

public class ExtToken {
	public static int Prefix = 1 << 1;
	public static int Suffix = 1 << 2;
	
	public int type;
	public CharVector token;
	
	public ExtToken(int type, CharVector token) {
		this.type = type;
		this.token = token;
	}

	public static ExtToken getExtToken(CharVector token) {
		int lastIndex = token.start() + token.length() - 1;
		if(lastIndex > 0 && token.array()[lastIndex] == '*'){
			//마지막이 *로 끝나는 단어면 길이를 하나줄인다.
			token.setLength(token.length() - 1);
			return new ExtToken(Prefix, token);
		}else if(token.length() > 0 && token.charAt(0) == '*'){
			//처음이 *로 시작하는 단어면 시작위치를 하나 증가시킨다.
			token.setStart(token.start() + 1);
			return new ExtToken(Suffix, token);
		}
		
		return null;
	}
	
}
