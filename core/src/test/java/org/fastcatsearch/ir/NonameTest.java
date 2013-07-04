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

package org.fastcatsearch.ir;
import java.io.IOException;

import junit.framework.TestCase;


public class NonameTest extends TestCase{
	public void test1() throws IOException{
		pos = 0;
		int i = 108;
		writeVariableByte(i);
		pos = 0;
		int j = readVariableByte();
		System.out.println(i+","+j);
		assertEquals(i, j);
		
	}
	
	byte[] array = new byte[128];
	int pos;
	
	private void writeByte(int b){
		array[pos++] = (byte) b;
		System.out.println("w>> "+array[pos]);
	}
	private int readByte(){
		System.out.println("r<< "+array[pos]);
		return array[pos++];
	}
	public int writeVariableByte(int v) throws IOException {
		int byteCnt=0;
		do{
			int b = (byte)(v & 0x7F); //하위 7비트 
			v >>>= 7; //오른쪽으로 7비트 shift
			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
				b |= 0x80;
				writeByte(b);
			}else{
				writeByte(b);
			}
			byteCnt++;
		} while (v != 0);
		
		return byteCnt;
	}
	
	public int readVariableByte() throws IOException {
		int v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
}
