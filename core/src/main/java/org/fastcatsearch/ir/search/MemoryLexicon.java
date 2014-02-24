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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MemoryLexicon {
	private static Logger logger = LoggerFactory.getLogger(MemoryLexicon.class);
	
	//memory lexicon
	private char[][] terms;
	private long[] pointer; //position for lexicon file
	private long[] pos; //position for posting file
	private int size;
	
	public MemoryLexicon(int size){
		terms = new char[size][];
		pointer = new long[size];
		pos = new long[size];
		this.size = size;
	}
	
	public int size(){
		return size;
	}
	
	public long getStartPointer(){
		if(size > 0)
			return pointer[0];
		
		return -1;
	}
	
	public void put(int i, char[] term, long pointer, long pos){
		terms[i] = term;
		this.pointer[i] = pointer;
		this.pos[i] = pos;
	}
	
	public String toString(int i){
		return new String(terms[i])+" : "+pointer[i]+","+pos[i];
	}
	
	public boolean binsearch(CharVector singleTerm, long[] position){
		if(size == 0)
			return false;
		
    	int left = 0;
    	int right = size - 1;
    	int mid = -1;
    	
    	boolean found = false;
    	
    	while(left <= right){
    		mid = (left + right) / 2;

    		int cmp = compareKey(terms[mid], singleTerm);
    		
    		if(cmp == 0){
    			found = true;
    			break;
    		}else if(cmp < 0){
    			left = mid + 1;
    		}else{
    			right = mid - 1;
    		}
    	}
    	
    	if(found){
    		position[0] = pointer[mid];
//    		logger.info("binsearch pos["+mid+"] = "+pos[mid]);
    		position[1] = pos[mid];
    		return true;
    	}
    	
		//mid = Min(mid, right)
    	mid = right < mid ? right : mid;
    	
    	if(mid == -1)
    		mid = 0;
    	
    	if(mid > 0 && compareKey(terms[mid], singleTerm) > 0){
    		mid--;
    	}
    	if(mid < 0) mid = 0;
    	
		position[0] = pointer[mid];
		position[1] = -1;
		return false;
	}
	
	public boolean binsearchNumeric(CharVector singleTerm, long[] position){
		if(size == 0)
			return false;
		
    	int left = 0;
    	int right = size - 1;
    	int mid = -1;
    	
    	boolean found = false;
    	
    	while(left <= right){
    		mid = (left + right) / 2;
    		int cmp = compareNumericKey(terms[mid], singleTerm);
    		
    		if(cmp == 0){
    			found = true;
    			break;
    		}else if(cmp < 0){
    			left = mid + 1;
    		}else{
    			right = mid - 1;
    		}
    		
    	}
    	
    	if(found){
    		position[0] = pointer[mid];
    		position[1] = pos[mid];
    		return true;
    	}
		//mid = Min(mid, right)
    	mid = right < mid ? right : mid;
    	if(mid > 0 && compareNumericKey(terms[mid], singleTerm) > 0){
    		mid--;
    	}
    	if(mid < 0) mid = 0;
    	
		position[0] = this.pointer[mid];
		position[1] = -1;
		return false;
	}
	
	private int compareKey(char[] t, CharVector term){
//    	logger.info("compareKey => "+new String(t)+" , "+term);
    	int len1 = t.length;
    	int len2 = term.length();
    	
    	int len = len1 < len2 ? len1 : len2;
    	
    	for (int i = 0; i < len; i++) {
    		char ch = term.charAt(i);
    		
    		if(t[i] != ch){
    			return t[i] - ch;
    		}
    	}
    	
    	return len1 - len2;
    }
    
	private int compareNumericKey(char[] t, CharVector term){
    	int len1 = t.length;
    	int len2 = term.length();
    	
    	if(len1 != len2)
    		return len1 - len2;
    	
    	for (int i = 0; i < len1; i++) {
    		char ch = term.charAt(i);
    		
    		if(t[i] != ch){
    			return t[i] - ch;
    		}
    	}
    	
    	return 0;
    }
	
	
}
