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

public class TokenVector extends CharVector{
	public int tokenCount;
	private static int BUFFER_SIZE = 1024 * 4;
	public int[] tokenStart = new int[BUFFER_SIZE];
	public int[] tokenLength = new int[BUFFER_SIZE];
	public int[] tokenType = new int[BUFFER_SIZE];
	
	public void put(int start, int len, int type){
		tokenStart[tokenCount] = start;
		tokenLength[tokenCount] = len;
		tokenType[tokenCount] = type;
		if(tokenCount >= BUFFER_SIZE - 100){
			try{
				System.out.println(new String(array, start, len));
			}catch(StringIndexOutOfBoundsException e){
				System.out.println("array.length = "+array.length+", start="+start+", len="+len);
				System.out.println(new String(array));
				throw e;
			}
		}
		tokenCount++;
	}
	
	public String toString(){
		String result = "";
		for (int i = 0; i < tokenCount; i++) {
			result = result + new String(array, tokenStart[i], tokenLength[i]) + " "+tokenType[i]+"\n";
		}
		return "TokenCount:"+tokenCount+"\n"+result;
	}

	public void clear() {
		tokenCount = 0;
	}
}
