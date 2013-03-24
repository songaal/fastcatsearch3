/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.keyword;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 */
public class MemoryKeyword {
	private static Logger logger = LoggerFactory.getLogger(MemoryKeyword.class);
	
	private static final int MAX_RANK = 999;
	
	private int[] bucket;
	private char[] keyArray;
	private int[] keyPos;
	private int[] nextIdx;
	private int[] hit;
	private int[] popular;
	private int[] prevRank;
	private long[] dateRegister;
	private long[] dateUpdate;
	private boolean[] isOld;
				
	private int bucketSize;
	private int length;
	private int count;
	private int keyArrayLength;
	private int keyUseLength;
	
	public MemoryKeyword(){
		this(16 * 1024);
	}
	public MemoryKeyword(int size){
		bucketSize = size;
		length = bucketSize;
		count = 0;
		keyArrayLength = bucketSize * 5;
		keyUseLength = 0;
		
		bucket = new int[bucketSize];
		keyArray = new char[keyArrayLength];
		keyPos = new int[length];
		nextIdx = new int[length];
		hit = new int[length];
		popular = new int[length];
		prevRank = new int[length];
		dateRegister = new long[length];
		dateUpdate = new long[length];
		
		Arrays.fill(bucket, -1);
	}
	
	private String getKey(int id){
		int pos = keyPos[id];
		int len = -1;

		if(id == count - 1)
			len = keyUseLength - pos;
		else
			len = keyPos[id+1] - pos;
		return new String(keyArray, pos , len);
	}
	
	public Iterator<KeywordHit> getIterator(int size) {
		final int limit = (size > count) ? count : size;
		
		//sort
		final int[] sortedID = new int[count];
		for (int i = 0; i < count; i++)
			sortedID[i] = i;
		if(count > 0){
			long st = System.currentTimeMillis();
			logger.debug("MemoryKeyword term sort...");
			quickSort(sortedID, 0, count - 1);
			logger.debug("Sort Done. time = "+(System.currentTimeMillis() - st)+"ms");
		}
		
//		logger.debug("count = "+count);
		
		
		return new Iterator<KeywordHit>(){
			
			int pos = 0;
			
			public boolean hasNext() {
				return pos < limit;
			}

			public KeywordHit next() {
				
				int id = sortedID[pos];
				int p = keyPos[id];
				int len = -1;
				//마지막 원소이면
				if(id == count - 1)
					len = keyUseLength - p;
				else
					len = keyPos[id+1] - p;
				
//				logger.debug(new String(keyArray, p, len) + " : "+hit[id]);
				
				pos++;
				
				return new KeywordHit(pos, new String(keyArray, p, len), hit[id], popular[id], prevRank[id], true, new Date(dateRegister[id]), new Date(dateUpdate[id]));
			}

			public void remove() {
				
			}
			
		};
		
	}
	
	public Iterator<KeywordFail> getIteratorFail(int size) {
		final int limit = (size > count) ? count : size;
		
		//sort
		final int[] sortedID = new int[count];
		for (int i = 0; i < count; i++)
			sortedID[i] = i;
		if(count > 0){
			long st = System.currentTimeMillis();
			logger.debug("MemoryKeyword term sort...");
			quickSort(sortedID, 0, count - 1);
			logger.debug("Sort Done. time = "+(System.currentTimeMillis() - st)+"ms");
		}
		
//		logger.debug("count = "+count);
		
		
		return new Iterator<KeywordFail>(){
			
			int pos = 0;
			
			public boolean hasNext() {
				return pos < limit;
			}

			public KeywordFail next() {
				
				int id = sortedID[pos];
				int p = keyPos[id];
				int len = -1;
				//마지막 원소이면
				if(id == count - 1)
					len = keyUseLength - p;
				else
					len = keyPos[id+1] - p;
				
//				logger.debug(new String(keyArray, p, len) + " : "+hit[id]);
				
				pos++;
				
				return new KeywordFail(pos, new String(keyArray, p, len), hit[id], popular[id], prevRank[id], true, new Date(dateRegister[id]), new Date(dateUpdate[id]));
			}

			public void remove() {
				
			}
			
		};
		
	}
	
	private void quickSort(int[] ids, int first, int last) {
		if(last <= 0)
			return;
		
		int stackMaxSize = (int) ((Math.log(last - first + 1) + 3) * 2);
		int[][] stack = new int[stackMaxSize][2];
		
		int pivotId = 0, sp = 0;
		int left = 0, right = 0;
		
		while(true){
			while(first < last){
			    left = first;
			    right = last;
			    int median = (left + right)/2;
				
				//move pivot to left most. 
				int tmp = ids[left];
				ids[left] = ids[median];
				ids[median] = tmp;
				pivotId = ids[left];
				
			    while (left < right) {
			    	while (comparePopular(ids[right], pivotId) <= 0 && (left < right)) 
			            right --;
			    	
			        if (left != right){ 
			             ids[left] = ids[right];
			             left++;
			        } 
			        
			        while (comparePopular(ids[left], pivotId) >= 0 && (left < right)) 
			            left ++;
			        
			        if (left != right) {
			             ids[right] = ids[left];
			             right --;
			        } 
			    } 
			    
			    ids[left] = pivotId;
			    
			    if(left - first < last - left){
				    if (left + 1 < last) {
				    	sp++;
				    	stack[sp][0] = left + 1;
						stack[sp][1] = last;
				    }
				    last = left - 1;
			    }else{
			    	if (first < left - 1) {
				    	sp++;
				    	stack[sp][0] = first;
						stack[sp][1] = left -1;
				    }
				    first = left + 1;
			    }
			    
			}
			
			if (sp == 0) {
				return;
			}else {
				first = stack[sp][0];
				last = stack[sp][1];
				sp--;
			}
			
		}
		
	}
	
	
	private int compareKey(int id, int id2){
		int pos = keyPos[id];
		int len = -1;

		if(id == count - 1)
			len = keyUseLength - pos;
		else
			len = keyPos[id+1] - pos;
		
		int pos2 = keyPos[id2];
		int len2 = -1;

		if(id2 == count - 1)
			len2 = keyUseLength - pos2;
		else
			len2 = keyPos[id2+1] - pos2;
		
		int length = (len < len2) ? len : len2;

		for (int i = 0; i < length; i++) {
			if(keyArray[pos+i] != keyArray[pos2+i])
				return keyArray[pos+i] - keyArray[pos2+i];
		}

		return len - len2;
	}
	
	private int compareHit(int id, int id2){
		return hit[id] - hit[id2];
	}
	
	private int comparePopular(int id, int id2) {
		return popular[id] - popular[id2];
	}
	
	public void put(String term) {
		if(term.length() > 50){
			logger.warn("Search Keyword length is greater than 50. skip = "+term);
			return;
		}
		int h = getHit(term);
		put(term, h + 1, h + 1, 999, 0, 0);
	}
	
	public void add(String term, int addHit, int popular, int prevRank, long dateRegister, long dateUpdate) {
		if(term.length() > 50){
			logger.warn("Search Keyword length is greater than 50. skip = "+term);
			return;
		}
		int h = getHit(term);
		put(term, h + addHit, popular, prevRank, dateRegister, dateUpdate);
	}
	
	private boolean isTheSame(String term, int id) {
		int pos = keyPos[id];
		int len = -1;
		//last el?
		if(id == count - 1)
			len = keyUseLength - pos;
		else
			len = keyPos[id+1] - pos;
		
		if(term.length() == len){
			for (int i = 0; i < len; i++) {
				if(term.charAt(i) != keyArray[pos+i])
					return false;
			}
			return true;
		}
		return false;
	}
	
	public void setValues(int idx, int p, int p2, int p3, long p4, long p5) {
		hit[idx] = p;
		popular[idx] = p2;
		prevRank[idx] = p3;
		dateRegister[idx] = p4;
		dateUpdate[idx] = p5;
	}
	
	public int put(String term, int p, int p2, int p3, long p4, long p5) {
		int hashValue = rsHash(term);
		
		int prev = -1;
		int idx = bucket[hashValue];

		while(idx >= 0){
			if(isTheSame(term, idx))
				break;
			
			prev = idx;
			idx = nextIdx[idx];
		}
		
		if(idx >= 0){
			//duplicated term 
			if(prev != -1){
				//put a link to the front
				nextIdx[prev] = nextIdx[idx];
				nextIdx[idx] = bucket[hashValue];
				bucket[hashValue] = idx;
			}//else let it be
		}
		else{
			//new term
			idx = getNextIdx();
			
			if (keyUseLength + term.length() >= keyArrayLength) {
				keyArrayLength *= 1.2;
				char[] newArray = new char[keyArrayLength];
				System.arraycopy(keyArray, 0, newArray, 0, keyUseLength);
				keyArray = newArray;
			}
			keyPos[idx] = keyUseLength;
			
			for (int i=0; i < term.length(); i++) {
				keyArray[keyUseLength++] = term.charAt(i);
			}
			
			nextIdx[idx] = -1;
			if(prev != -1)
				nextIdx[prev] = idx;
			else
				bucket[hashValue] = idx;
			
		}
		
		if(p4 == 0) { p4 = new Date().getTime(); }
		if(p5 == 0) { p5 = new Date().getTime(); }
		
		int old = hit[idx];
		hit[idx] = p;
		popular[idx] = p2;
		prevRank[idx] = p3;
		dateRegister[idx] = p4;
		dateUpdate[idx] = p5;
		
		return old;
	}
	
	public int getId(String term) {
		int hashValue = rsHash(term);
		int idx = bucket[hashValue];
		
//		logger.debug(term+" = "+hashValue+", idx="+idx);
		while(idx >= 0){
			if(isTheSame(term, idx))
				break;
			
			idx = nextIdx[idx];
		}
		
		return idx;
	}

	public int getHit(String term) { return getHit(getId(term)); }
	public int getHit(int idx) {
		if(idx < 0) {
			return 0; //검색실패 
		} else {
			return hit[idx];
		}
	}
	
	public int getPopular(String term) { return getPopular(getId(term)); }
	public int getPopular(int idx) {
		if(idx < 0) {
			return 0; 
		} else {
			return popular[idx];
		}
	}
	
	public int getRank(String term) { return getRank(getId(term)); }
	public int getRank(int idx) {
		if(idx < 0) {
			return 0;
		} else {
			return prevRank[idx];
		}
	}
	
	private int getNextIdx() {
		if(count >= length){
			int newLength = (int) (length * 1.2);
//			logger.debug("Grow length = "+length+" => "+newLength+", new int * 2, new PostingBuffer[], arraycopy * 3");
			int[] newKeyPos = new int[newLength];
			int[] newNext = new int[newLength];
			int[] newHit = new int[newLength];
			int[] newPopular = new int[newLength];
			int[] newPrevRank = new int[newLength];
			long[] newDateRegister = new long[newLength];
			
			System.arraycopy(keyPos, 0, newKeyPos, 0, count);
			System.arraycopy(nextIdx, 0, newNext, 0, count);
			System.arraycopy(hit, 0, newHit, 0, count);
			System.arraycopy(popular, 0, newPopular, 0, count);
			System.arraycopy(prevRank, 0, newPrevRank, 0, count);
			System.arraycopy(dateRegister, 0, newDateRegister, 0, count);
			
			keyPos = newKeyPos;
			nextIdx = newNext;
			hit = newHit;
			popular = newPopular;
			prevRank = newPrevRank;
			dateRegister = newDateRegister;
			length = newLength;
		}
		return count++;
	}
	
	private int rsHash(String term) {
		int b = 378551;
		int a = 63689;
		int hashValue = 0;

		for(int i=0; i < term.length(); i++) {
			hashValue = hashValue * a + term.charAt(i);
			a = a * b;
		}
		return hashValue & (bucketSize - 1);
	}

	public int workingMemorySize(){
		return keyUseLength * 2 + hit.length * 4;
	}
	
	public int staticMemorySize(){
		int size = 0;
		size += keyArrayLength * 2;
		size += bucket.length * 4;
		size += keyPos.length * 4;
		size += nextIdx.length * 4;
		size += hit.length * 4;
		
		return size;
	}
	
	public void clear(){
		Arrays.fill(bucket, -1);
		Arrays.fill(nextIdx, -1);
		count = 0;
		keyUseLength = 0;
	}
	
	//entry count
	public int count() {
		return count;
	}

	/** calculate current popular rate from current hit **/
	public void calcPopular(int totHit) {
		for(int i=0;i<count;i++) {
			popular[i] = (int)(hit[i] / (totHit + 0.01) * 10000);
		}
	}
}

