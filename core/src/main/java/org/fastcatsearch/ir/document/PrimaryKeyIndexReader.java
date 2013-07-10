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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.IndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class PrimaryKeyIndexReader implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexReader.class);
	private static final Lock lock = new ReentrantLock();
	
	private IndexInput input;
	private IndexInput indexInput;
	private byte[][] keys;
	private long[] pos;
	private int count; //key 갯수 
	private long limit;
	
	public PrimaryKeyIndexReader(File dir, String filename) throws IOException{
		this(dir, filename ,0, -1, 0);
	}
	public PrimaryKeyIndexReader(IndexInput input, byte[][] keys, long[] pos, int count, long limit) {
		this.input = input;
		this.keys = keys;
		this.pos = pos;
		this.count = count;
		this.limit = limit;
	}
	public PrimaryKeyIndexReader(File dir, String filename, long dataBasePosition, long dataEndPosition, long indexBasePosition) throws IOException{
		input = new BufferedFileInput(dir, filename);
//		logger.debug("input = "+input+", size="+input.size());
		input.seek(dataBasePosition);
		indexInput = new BufferedFileInput(dir, filename+".index");
		indexInput.seek(indexBasePosition);
		
		if(dataEndPosition < 0)
			limit = input.length();
		else
			limit = dataEndPosition;
		
		this.count = input.readInt();
//		logger.debug("** count="+count);
		int idxCount = indexInput.readInt();
//		logger.debug("** idxCount="+idxCount);
		keys = new byte[idxCount][];
		pos = new long[idxCount];
		
		for (int i = 0; i < idxCount; i++) {
			int len = indexInput.readVInt();
//			logger.debug("len="+len);
			keys[i] = new byte[len];
			indexInput.readBytes(keys[i], 0, len);
			pos[i] = indexInput.readLong();
		}
		
		indexInput.close();
//		logger.debug("idxCount="+idxCount);
	}
	
	public PrimaryKeyIndexReader clone(){
		return new PrimaryKeyIndexReader(input.clone(), keys, pos, count, limit);
	}
	public int count(){
		return count;
	}
	
	public void close() throws IOException{
		input.close();
	}
	
	public int get(byte[] data) throws IOException{
		return get(data, 0, data.length);
	}
	
	public int get(BytesBuffer bytesBuffer) throws IOException{
		return get(bytesBuffer.bytes, bytesBuffer.offset, bytesBuffer.length);
	}
	
	
	public int get(byte[] data, int offset, int length) throws IOException{
		//if index is empty, it has no entry.
		if(keys.length == 0)
			return -1;
		
		int idx = binsearch(data, offset, length);
		byte[] test = new byte[length];
//		long position = pos[idx] + dataBasePosition;
		long position = pos[idx];
//		logger.debug("input ="+input.size()+", position="+position+", length="+length + ", "+pos[idx]+", "+dataBasePosition);
		try{
			lock.lock();
			input.seek(position);
			
			while(input.position() < limit){
				int len = input.readVInt();
	//			if(length == len){
				if(length < len){
					test = new byte[len];
				}
				input.readBytes(test, 0, len);
				int docNo = input.readInt();
				int ret = compare(test, len, data, offset, length);
//				logger.debug("compare = "+new String(test, 0, len)+" : "+new String(data, offset, length)+" docNo="+docNo+", ret="+ret);
				if(ret == 0) 
					return docNo;
				else if(ret > 0)
					return -1;
	//			}
			}
		}finally{
			lock.unlock();
		}
		//get EOF
		return -1;
	}
	
	//find closest smaller one's position
	private int binsearch(byte[] data, int offset, int length){
		int left = 0;
		int right = keys.length - 1;
		int mid = -1;
		
		while(left <= right){
			mid = (int) ((left + right) / 2);
			int ret = compare(keys[mid], keys[mid].length,data, offset, length);
			if(ret == 0){
				return mid;
			}
			
			else if(ret < 0)
				left = mid + 1;
			else 
				right = mid - 1;
		}
		
		mid = right < mid ? right : mid;
		if(mid < 0)
			mid = 0;
		
		if (compare(keys[mid], keys[mid].length, data, offset, length) > 0 && mid > 0) {
			mid--;
    	}
		
    	return mid;
		
	}
	
	private int compare(byte[] key, int keyLen, byte[] data, int offset, int length){
		int len = keyLen < length ? keyLen : length; 
//		logger.debug("keyLen={}, length={}", keyLen, length);
		for (int i = 0; i < len; i++) {
			if(key[i] != data[offset + i]){
//				logger.debug("cmp >> {}:{}", key[i], data[offset + i]);
				return key[i] - data[offset + i];
			}
		}
		
		if(length > keyLen){
			int remain = length - keyLen;
			for (int i = 0; i < remain; i++) {
				if(data[offset + len + i] != 0){
					//남은 데이터가 0이 아니면 다른 데이터이다.
					return keyLen - length;	
				}else{
					//남은 데이터가 모두 0이라면 무시해도 좋다.
				}
			}
		}
		//여기까지오면 같은 데이터라고 볼수 있다. 
		return 0;
	}
	
}
