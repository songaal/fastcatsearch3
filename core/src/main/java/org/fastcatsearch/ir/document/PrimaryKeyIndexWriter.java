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

package org.fastcatsearch.ir.document;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * 해시구조로 키들을 모은후 정렬하여 파일에 쓴다. 
 * @author sangwook.song
 *
 */
public class PrimaryKeyIndexWriter {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexWriter.class);
	
	private IndexOutput output;
	private IndexOutput indexOutput;
	private int[] bucket;
	private byte[] array;
	private int[] keyPos;
	private int[] nextIdx;
	private int[] intValueArray;
	private int bucketSize;
	private int length;
	private int count;
	private int keySize;
	private int keyUseSize;
	private int indexInterval;
	private boolean hasOutOfMemory;
	
	public PrimaryKeyIndexWriter(int bucketSize) throws IOException{
		this(null, null, 0, bucketSize);
	}
	public PrimaryKeyIndexWriter(int indexInterval, int bucketSize) throws IOException{
		this(null, null, indexInterval, bucketSize);
	}
	public PrimaryKeyIndexWriter(File dir, String filename, int indexInterval, int bucketSize) throws IOException{
		
		
		if(dir != null && filename != null){
			String indexFilename = IndexFileNames.getIndexFileName(filename);
			output = new BufferedFileOutput(dir, filename);
			indexOutput = new BufferedFileOutput(dir, indexFilename);
		}
		this.indexInterval = indexInterval;
		this.bucketSize = bucketSize;
		length = bucketSize;
		count = 0;
		keySize = bucketSize * 5;
		keyUseSize = 0;
//		logger.debug("length="+length);
		bucket = new int[bucketSize];
		array = new byte[keySize];
		keyPos = new int[length];
		nextIdx = new int[length];
		intValueArray = new int[length];

		Arrays.fill(bucket, -1);
		Arrays.fill(intValueArray, -1);
	}
	
	public void setDestination(IndexOutput output, IndexOutput indexOutput){
		this.output = output;
		this.indexOutput = indexOutput;
	}
	
	public int count(){
		return count;
	}
	
	public void write() throws IOException{
		if(hasOutOfMemory)
			return;
		
		if(count == 0){
			output.writeInt(0);
			indexOutput.writeInt(0);
			return;
		}
		
		//sort
		int[] sortedIdx = new int[count];
		for (int i = 0; i < count; i++)
			sortedIdx[i] = i;
		
		long st = System.currentTimeMillis();
		quickSort(sortedIdx, 0, count - 1);
		logger.debug("sort time = "+(System.currentTimeMillis() - st)+"ms");
		//term count
		output.writeInt(count);
//		long indexPos = indexOutput.position();
		indexOutput.writeInt(0);//write later again.
		logger.debug("pk count = {}", count);
		int idxCount = 0;
		
		for (int i = 0; i < count; i++) {
			int id = sortedIdx[i];
			int pos = keyPos[id];
			int len = -1;
			//last elt
			if(id == count - 1)
				len = keyUseSize - pos;
			else
				len = keyPos[id+1] - pos;
			
			//write pkmap index
			if(indexInterval > 0 && i % indexInterval == 0){
				indexOutput.writeVInt(len);
				indexOutput.writeBytes(array, pos, len);
				indexOutput.writeLong(output.position());
				idxCount++;
			}
			
			output.writeVInt(len);
			output.writeBytes(array, pos, len);
			output.writeInt(intValueArray[id]);
		}
		
		logger.debug("{} pk index count = {}, filesize = {} bytes", output.toString(), idxCount, output.position());
	
		//write idxCount
//		long p = indexOutput.position();
		indexOutput.seek(0);
		indexOutput.writeInt(idxCount);
//		indexOutput.seek(p);
	}
	
	public void close() throws IOException{
		indexOutput.close();
		output.close();
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
			    int pivot = (left + right)/2;
				
				//move pivot to left most. 
				int tmp = ids[left];
				ids[left] = ids[pivot];
				ids[pivot] = tmp;
				pivotId = ids[left];
				
			    while (left < right) {
			    	while (compareKey(ids[right], pivotId) >= 0 && (left < right)) 
			            right --;
			    	
			        if (left != right){ 
			             ids[left] = ids[right];
			             left++;
			        } 
			        
			        while (compareKey(ids[left], pivotId) <= 0 && (left < right)) 
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
		//last elt
		if(id == count - 1)
			len = keyUseSize - pos;
		else
			len = keyPos[id+1] - pos;
		
		int pos2 = keyPos[id2];
		int len2 = -1;
		//last elt
		if(id2 == count - 1)
			len2 = keyUseSize - pos2;
		else
			len2 = keyPos[id2+1] - pos2;
		
		int length = (len < len2) ? len : len2;
		
		for (int i = 0; i < length; i++) {
			if(array[pos+i] != array[pos2+i])
//				return (array[pos+i] & 0xFF) - (array[pos2+i] & 0xFF);
				return array[pos+i] - array[pos2+i];
		}
		
		return len - len2;
	}
	
	private boolean isTheSame(byte[] data, int offset, int dataLength, int idx) {
		int pos = keyPos[idx];
		int len = -1;

		if(idx == count - 1)
			len = keyUseSize - pos;
		else
			len = keyPos[idx+1] - pos;
		
		if(dataLength == len){
			for (int i = 0; i < len; i++) {
				if(data[offset + i] != array[pos+i])
					return false;
			}
			return true;
		}
		return false;
	}
	
	public int put(BytesBuffer buffer, int docNo) throws IOException {
		return put(buffer.bytes, buffer.offset, buffer.length(), docNo);
	}
	public int put(byte[] data, int offset, int dataLength, int docNo) throws IOException {
		int hashValue = rsHash(data, offset, dataLength);
		int prev = -1;
		int idx = bucket[hashValue];

		while(idx >= 0){
			if(isTheSame(data, offset, dataLength, idx)){
				break;
			}
			prev = idx;
			idx = nextIdx[idx];
		}
		
		if(idx >= 0){
			if(prev != -1){
				nextIdx[prev] = nextIdx[idx];
				nextIdx[idx] = bucket[hashValue];
				bucket[hashValue] = idx;
			}
		}
		else{
			//new term
			idx = getNextIdx();
//			logger.debug("new term next idx = "+idx+"/ "+dataLength+" / "+keyUseSize);
			
			try{
				if (keyUseSize + dataLength >= keySize) {
					keySize *= 1.2;
//					logger.debug(this+" ## grow keysize = "+keySize+", "+Runtime.getRuntime().totalMemory()+", count="+count+", dl = "+dataLength);
					
					byte[] newArray = new byte[keySize];
					System.arraycopy(array, 0, newArray, 0, keyUseSize);
					array = newArray;
				}
			}catch(OutOfMemoryError e){
				hasOutOfMemory = true;
				logger.error("PK writing OOM! size = "+keySize+" msg = "+e.getMessage(),e);
				throw new IOException(e.toString());
			}
			
			keyPos[idx] = keyUseSize;
			System.arraycopy(data, offset, array, keyUseSize, dataLength);
			keyUseSize += dataLength;
			
			nextIdx[idx] = -1;
			if(prev != -1)
				nextIdx[prev] = idx;
			else
				bucket[hashValue] = idx;
		}
		
		int old = intValueArray[idx];
		intValueArray[idx] = docNo;
		
		return old;
	}
	public int get(BytesBuffer buffer) {
		return get(buffer.bytes, buffer.offset, buffer.length());
	}
	public int get(byte[] data, int offset, int dataLength) {
		int hashValue = rsHash(data, offset, dataLength);
		int idx = bucket[hashValue];
		
		while(idx >= 0){
			if(isTheSame(data, offset, dataLength, idx))
				break;
			
			idx = nextIdx[idx];
		}
		if(idx >= 0)
			return intValueArray[idx];
		else{
			return -1;
		}
	}

	private int getNextIdx() {
		if(count >= length){
			int newLength = (int) (length * 1.2);
			int [] newKeyPos = new int[newLength];
			int [] newNext = new int[newLength];
			int [] newIntValueArray = new int[newLength];
			
			System.arraycopy(keyPos, 0, newKeyPos, 0, count);
			System.arraycopy(nextIdx, 0, newNext, 0, count);
			System.arraycopy(intValueArray, 0, newIntValueArray, 0, count);
			Arrays.fill(newIntValueArray, count, newIntValueArray.length, -1);
			
			keyPos = newKeyPos;
			nextIdx = newNext;
			intValueArray = newIntValueArray;
			length = newLength;
		}
		return count++;
	}
	
	private int rsHash(byte[] data, int offset, int length) {
		int b = 378551;
		int a = 63689;
		int hashValue = 0;

		for (int i = 0; i < length; i++) {
			hashValue = hashValue * a + (data[offset + i] & 0xff);
			a = a * b;
		}
		return hashValue & (bucketSize - 1);
	}
	
}
