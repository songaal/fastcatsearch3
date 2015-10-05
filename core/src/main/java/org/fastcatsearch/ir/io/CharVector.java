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
	
	private char[] array;
	private int start;
	private int length;
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
	public CharVector(char[] array, boolean isIgnoreCase){
		this(array, 0, array.length, isIgnoreCase);
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
	
	public void init(int start, int length) {
		this.start = start;
		this.length = length;
		this.hash = 0;
	}

	public void setStart(int start) {
		this.start = start;
		this.hash = 0;
	}
	
	public void setLength(int length){
		this.length = length;
		this.hash = 0;
	}
	public void setIgnoreCase(){
		if(!isIgnoreCase){
			this.isIgnoreCase = true;
			hash = 0;
		}
	}
	public void unsetIgnoreCase(){
		if(isIgnoreCase){
			this.isIgnoreCase = false;
			hash = 0;
		}
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
			ch = toUpperChar(ch);
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
		if(length > 0) {
			return new String(array, start, length);
		} else {
			return "";
		}
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
				
				if(isIgnoreCase || anotherArray.isIgnoreCase){
					//둘중 하나라도 ignorecase이면 ignorecase로 비교한다.
					for (int i = 0; i < length; i++) {
						if (toUpperChar(charAt(i)) != toUpperChar(anotherArray.charAt(i))) {
							return false;
						}
					}
					
					
				}else{
					for (int i = 0; i < length; i++) {
						if (charAt(i) != anotherArray.charAt(i)) {
							return false;
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	
	// share array reference
	@Override
	public CharVector clone() {
		CharVector c = new CharVector(array, start, length, isIgnoreCase);
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
		return new CharVector(buffer, 0, length, isIgnoreCase);
	}

//	public CharVector toUpperCase() {
//		for (int i = start, end = start + length; i < end; i++) {
//			if ('a' <= array[i] && array[i] <= 'z') {
//				array[i] = (char) (array[i] - 32);
//			}
//		}
//		hash = 0;
//		return this;
//	}
//
//	public CharVector toLowerCase() {
//		for (int i = start, end = start + length; i < end; i++) {
//			if ('A' <= array[i] && array[i] <= 'Z') {
//				array[i] = (char) (array[i] + 32);
//			}
//		}
//		hash = 0;
//		return this;
//	}

	@Override
	public int compareTo(CharSequence cs) {

		int len1 = this.length;
		int len2 = cs.length();

		int minlen = len1;

		if (minlen > len2) {
			minlen = len2;
		}

		for (int cinx = 0; cinx < minlen; cinx++) {
			char c1 = this.charAt(cinx);
			char c2 = cs.charAt(cinx);

			if (c1 == c2) {

			} else if (c1 > c2) {
				return 1;
			} else if (c1 < c2) {
				return -1;
			}
		}

		if (len1 == len2) {
			return 0;
		} else if (len1 > len2) {
			return 1;
		} else if (len1 < len2) {
			return -1;
		}
		return 0;
	}
	
	public int compareTo(char[] key, int offset, int length) {

		int len1 = this.length;
		int len2 = length;

		int len = len1 < len2 ? len1 : len2;

		for (int i = 0; i < len; i++) {
			char ch = charAt(i);

			if (ch != key[offset + i]) {
				return ch - key[offset + i];
			}
		}

		return len1 - len2;
	}

	@Override
	public char charAt(int inx) {
		char ch = array[start + inx];
		if (isIgnoreCase) {
			if ((ch <= 'z' && ch >= 'a')) { // 소문자이면..
				ch -= 32;
			}

		}
		return ch;
	}
	
	private char toUpperChar(int ch){
		if ((ch <= 'z' && ch >= 'a')) { // 소문자이면..
			ch -= 32;
		}
		return (char) ch;
	}
	
	public void setChar(int inx, char ch){
		array[start+inx] = ch;
		hash = 0;
	}

    //내부 공백을 삭제해준다.
    public CharVector removeWhitespaces() {
        int len = 0;
        for(int i = 0; i < length; i++) {
            if(array[start + i] != ' ') {
                array[start + len++] = array[start + i];
            }
        }
        length = len;
        hash = 0;
        return this;
    }

    public boolean hasWhitespaces() {
        for(int i = 0; i < length; i++) {
            if(array[start + i] == ' ') {
                return true;
            }
        }

        return false;
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
		return charAt(inx);
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
