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

import java.io.CharArrayReader;
import java.io.Reader;
import java.io.Serializable;

public class CharVector implements ElementVector, CharSequence, Comparable<CharSequence>, Serializable {
	private static final long serialVersionUID = -7987933270331385376L;
	
	public char[] array;
	public int start;
	public int length;
	protected int hash;

	private boolean isIgnoreCase;
	
	public CharVector() {
	}

	public CharVector(String str) {
		array = str.toCharArray();
		start = 0;
		length = array.length;
	}
	public CharVector(char[] array){
		this(array, 0, array.length);
	}
	public CharVector(char[] array, int start, int length) {
		this(array, start, length, false);
	}
	public CharVector(char[] array, int start, int length, boolean isIgnoreCase) {
		this.array = array;
		this.start = start;
		this.length = length;
		this.hash = 0;
		this.isIgnoreCase = isIgnoreCase;
	}
	
	public void init(char[] array, int start, int length) {
		this.array = array;
		this.start = start;
		this.length = length;
		this.hash = 0;
	}

	public void setIgnoreCase(){
		this.isIgnoreCase = true;
	}
	public void unsetIgnoreCase(){
		this.isIgnoreCase = false;
	}
	public boolean isIgnoreCase(){
		return isIgnoreCase;
	}
	
	//해시코드는 대소문자 구분없이 모두 대문자 기준으로 만들어준다.
	public int hashCode() {
		if(hash > 0){
			return hash;
		}
		int h = 0;
		int off = start;

		for (int i = 0; i < length; i++) {
			int ch = array[off++];
			if(ch >= 'a' && ch <= 'z'){
				ch -= 32;//to upper case 
			}
			h = 31 * h + ch;
		}
		hash = h;
		return h;
	}

	public CharVector trim() {

		while (length > 0 && array[start] == ' ') {
			start++;
			length--;
		}

		while (length > 0 && array[start + length - 1] == ' ') {
			length--;
		}
		hash = 0;
		return this;
	}

	public String toString() {
		return new String(array, start, length);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof CharVector) {
			CharVector anotherArray = (CharVector) anObject;
			int n = length;
			if (n == anotherArray.length) {
				char v1[] = array;
				char v2[] = anotherArray.array;
				int i = start;
				int j = anotherArray.start;
				
				if(isIgnoreCase || anotherArray.isIgnoreCase){
					//둘중 하나라도 ignorecase이면 ignorecase로 비교한다.
					while (n-- != 0) {
						if (v1[i] != v2[j]){
							if(((v1[i] <= 'z' && v1[i] >= 'a') || (v1[i] <= 'Z' && v1[i] >= 'A')) //v1이 영문자이거나.
									|| 
								((v2[j] <= 'z' && v2[j] >= 'a') || (v2[j] <= 'Z' && v2[j] >= 'A'))){ //v2가 영문자이면..
								if(Math.abs(v1[i] - v2[j]) != 32){
									return false;
								}
								//OK 대소문자 구분 없이 동일함.
							}else{
								return false;
							}
						}
						i++;j++;
					}
				}else{
					while (n-- != 0) {
						if (v1[i++] != v2[j++])
							return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
//	public boolean equalsIgnoreCase(Object anObject) {
//		if (anObject instanceof CharVector) {
//			CharVector anotherArray = (CharVector) anObject;
//			int n = length;
//			if (n == anotherArray.length) {
//				char v1[] = array;
//				char v2[] = anotherArray.array;
//				int i = start;
//				int j = anotherArray.start;
//				while (n-- != 0) {
//					int v1v=v1[i++];
//					int v2v=v2[j++];
//					if ( !(v1v==v2v || Math.abs(v1v-v2v)== 32) ) {
//						return false;
//					}
//				}
//				return true;
//			}
//		}
//		return false;
//	}

	// share array reference
	@Override
	public CharVector clone() {
		CharVector c = new CharVector(array, start, length);
		c.hash = hash;
		return c;
	}

	public void copy(char[] buffer){
		System.arraycopy(array, start, buffer, 0, length);
	}
	public void copy(CharVector another) {
		another.start = start;
		another.length = length;
		another.array = array;
		another.hash = hash;
		another.isIgnoreCase = isIgnoreCase;
	}
	public CharVector duplicate() {
		char[] buffer = new char[length];
		copy(buffer);
		return new CharVector(buffer, 0, length);
	}

	public CharVector toUpperCase() {
		for (int i = start, end = start + length; i < end; i++) {
			if ('a' <= array[i] && array[i] <= 'z') {
				array[i] = (char) (array[i] - 32);
			}
		}
		hash = 0;
		return this;
	}

	public CharVector toLowerCase() {
		for (int i = start, end = start + length; i < end; i++) {
			if ('A' <= array[i] && array[i] <= 'Z') {
				array[i] = (char) (array[i] + 32);
			}
		}
		hash = 0;
		return this;
	}

	@Override
	public int compareTo(CharSequence cs) {
		
		int len1 = this.length;
		int len2 = cs.length();
		
		int minlen = len1;
		
		if(minlen > len2) {
			minlen = len2;
		}
		
		for(int cinx=0;cinx<minlen;cinx++) {
			char c1 = this.charAt(cinx);
			char c2 = cs.charAt(cinx);
			
			if(c1 == c2) {
				
			} else if(c1 > c2) {
				return 1;
			} else if(c1 < c2) {
				return -1;
			}
		}
		
		if(len1 == len2) {
			return 0;
		} else if(len1 > len2) {
			return 1;
		} else if(len1 < len2) {
			return -1;
		}
		return 0;
	}

	@Override
	public char charAt(int inx) {
		return array[start+inx];
	}
	public void setChar(int inx, char ch){
		array[start+inx] = ch;
		hash = 0;
	}

	public char[] array() {
		return array;
	}
	
	public int start() {
		return start;
	}

	@Override
	public int length() {
		return length;
	}
	
	@Override
	public int elementAt(int inx) {
		return array[start+inx];
	}

	@Override
	public CharSequence subSequence(int startIndex, int endIndex) {
		CharVector cv = new CharVector();
		cv.array = this.array;
		cv.start = this.start + startIndex;
		cv.length = endIndex - startIndex + 1;
		return cv;
	}
	
	public Reader getReader(){
		return new CharArrayReader(array, start, length);
	}
}
