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

package org.fastcatsearch.ir.io;

public class ByteVector implements ElementVector {
	public byte[] array;
	public int start;
	public int length;

	public ByteVector(byte[] array, int start, int length){
		this.array = array;
		this.start = start;
		this.length = length;
	}
	
	public void init(byte[] array, int start, int length){
		this.array = array;
		this.start = start;
		this.length = length;
	}
	
	public boolean equals(Object anObject) {
		if (this == anObject) {
		    return true;
		}
		if (anObject instanceof ByteVector) {
			ByteVector anotherArray = (ByteVector)anObject;
		    int n = length;
		    if (n == anotherArray.length) {
				byte v1[] = array;
				byte v2[] = anotherArray.array;
				int i = start;
				int j = anotherArray.start;
				while (n-- != 0) {
				    if (v1[i++] != v2[j++])
					return false;
				}
				return true;
		    }
		}
		return false;
    }
	//array 레퍼런스는 같이 사용한다.
	public Object clone(){
		return new ByteVector(array, start, length);
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public int elementAt(int pos) {
		return array[start+pos];
	}
}
