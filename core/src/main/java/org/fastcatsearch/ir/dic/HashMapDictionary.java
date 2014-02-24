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

package org.fastcatsearch.ir.dic;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.fastcatsearch.al.HashFunctions;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class HashMapDictionary {
	private static final Logger logger = LoggerFactory.getLogger(HashMapDictionary.class);
	
	private static final HashFunctions hfunc = HashFunctions.RSHash;
	
//	private static HashMap<File, HashMapDictionary> container = new HashMap<File, HashMapDictionary>();
//	public static File SYNONYM_DIC;
//	
//	static{
//		IRConfig irconfig = IRSettings.getConfig();
//		if(irconfig != null){
//			SYNONYM_DIC = new File(IRSettings.path(irconfig.getString("synonym.dic.path")));
//		}
//	}
	
	private int[] bucket;
	private char[] keyArray;
	private int[] keyPos;
	private int[] nextIdx;
	private CharVector[][] termArray;
	
	private int bucketSize;
	private int length;
	private int count;
	private int keyArrayLength;
	private int keyUseLength;
	
	public File file;
	
//	public synchronized static HashMapDictionary getDictionary(File file) throws IRException{
//		return getDictionary(file, false);
//	}
	
//	public synchronized static HashMapDictionary getDictionary(File file, boolean reload) throws IRException{
//		HashMapDictionary dic = container.get(file);
//		if(reload || dic == null){
//			dic = new HashMapDictionary(file);
//			container.put(file, dic);
//		}
//		
//		return dic;
//	}
	
	
	public HashMapDictionary(int size){
		init(size);
	}
	
	public HashMapDictionary(File file) throws IRException{
		this.file = file;
		try {
			long st = System.currentTimeMillis();
			IndexInput input = new BufferedFileInput(file);
			bucketSize = input.readInt();
			count = input.readInt();
			keyUseLength = input.readInt();
			
			bucket = new int[bucketSize];
			keyArray = new char[keyUseLength];
			keyPos = new int[count];
			nextIdx = new int[count];
			termArray = new CharVector[count][];
			
			for (int i = 0; i < bucketSize; i++) {
				bucket[i] = input.readInt();
			}
			for (int i = 0; i < keyUseLength; i++) {
				keyArray[i] = input.readUChar();
			}
			for (int i = 0; i < count; i++) {
				keyPos[i] = input.readInt();
			}
			for (int i = 0; i < count; i++) {
				nextIdx[i] = input.readInt();
			}
			for (int i = 0; i < count; i++) {
				int c = input.readVInt();
				termArray[i] = new CharVector[c];
//				int totalLength = input.readVariableByte();
//				char[] v = new char[totalLength];
//				input.readUChars(v, totalLength);
//				termArray[i] = new CharVector[c];
//				int start = 0;
//				for (int j = 0; j < c; j++) {
//					int len = input.readVariableByte();
//					termArray[i][j] = new CharVector(v, start , len);
//					start += len;
//				}
				
				for (int j = 0; j < c; j++) {
					char[] charArr = input.readUString();
					termArray[i][j] = new CharVector(charArr, 0, charArr.length);
				}
			}
			input.close();
			logger.info("Load dictionary done! {}, entry = {}, time = {} ms", new Object[]{file.getAbsolutePath(), new Integer(count), System.currentTimeMillis() - st});
		} catch (IOException e) {
			logger.error("IOException",e);
			throw new IRException(e);
		}
		
	}
	
	public void save(File file) throws IRException{
		try {
			IndexOutput output = new BufferedFileOutput(file);
			output.writeInt(bucketSize);
			output.writeInt(count);
			output.writeInt(keyUseLength);
			
			for (int i = 0; i < bucketSize; i++) {
				output.writeInt(bucket[i]);
			}
			
			for (int i = 0; i < keyUseLength; i++) {
				output.writeUChar(keyArray[i]);
			}
			
			for (int i = 0; i < count; i++) {
				output.writeInt(keyPos[i]);
			}
			
			for (int i = 0; i < count; i++) {
				output.writeInt(nextIdx[i]);
			}
			
			for (int i = 0; i < count; i++) {
				//key has N entry
				output.writeVInt(termArray[i].length);
				for (int k = 0; k < termArray[i].length; k++) {
					output.writeUString(termArray[i][k].array(), termArray[i][k].start(), termArray[i][k].length());
				}
//				output.writeVariableByte(termArray[i].length);
//				int totalSize = 0;
//				for (int k = 0; k < termArray[i].length; k++) {
//					totalSize += termArray[i][k].length;
//				}
//				output.writeVariableByte(totalSize);
//				for (int k = 0; k < termArray[i].length; k++) {
//					output.writeUChars(termArray[i][k].array, termArray[i][k].start, termArray[i][k].length);
//				}
//				for (int k = 0; k < termArray[i].length; k++) {
//					output.writeVariableByte(termArray[i][k].length);
//				}
				
			}
			
			logger.info("Wrote {}, {}", Formatter.getFormatSize(output.length()), file.getAbsolutePath());
			output.close();
		} catch (IOException e) {
			logger.error("IOException",e);
			throw new IRException(e);
		}
	}
	
	public void init(int size){
		bucketSize = size;
		length = bucketSize;
		count = 0;
		keyArrayLength = bucketSize * 5;
		keyUseLength = 0;
		
		bucket = new int[bucketSize];
		keyArray = new char[keyArrayLength];
		keyPos = new int[length];
		nextIdx = new int[length];
		termArray = new CharVector[length][];
		
		Arrays.fill(bucket, -1);
	}
	
	private boolean isTheSame(CharVector term, int id) {
		int pos = keyPos[id];
		int len = -1;
		//last el?
		if(id == count - 1)
			len = keyUseLength - pos;
		else
			len = keyPos[id+1] - pos;
		
//		logger.debug("{} , term.length={}, len={}", term, term.length, len);
		if(term.length() == len){
			for (int i = 0; i < len; i++) {
				if(term.charAt(i) != keyArray[pos+i])
					return false;
			}
			return true;
		}
		return false;
	}
	
	public CharVector[] put(CharVector term, CharVector[] v) {
		if(v == null || v.length < 1)
			return null;
		
		int hashValue = hfunc.hash(term, bucketSize);
		
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
		
		CharVector[] old = termArray[idx];
		termArray[idx] = v;
		
		return old;
	}
	
	public CharVector[] get(CharVector term) {
		int hashValue = hfunc.hash(term, bucketSize);
		int idx = bucket[hashValue];
		
//		logger.debug("{} = {}, idx={}" ,term, hashValue, idx);
		while(idx >= 0){
			if(isTheSame(term, idx))
				break;
			
			idx = nextIdx[idx];
		}
		
		if(idx < 0)
			return null; //검색실패 
		else{
			return termArray[idx];
		}
	}
	
	private int getNextIdx() {
		if(count >= length){
			int newLength = (int) (length * 1.2);
			int[] newKeyPos = new int[newLength];
			int[] newNext = new int[newLength];
			CharVector[][] newTermArray = new CharVector[newLength][];
			
			System.arraycopy(keyPos, 0, newKeyPos, 0, count);
			System.arraycopy(nextIdx, 0, newNext, 0, count);
			System.arraycopy(termArray, 0, newTermArray, 0, count);
			
			keyPos = newKeyPos;
			nextIdx = newNext;
			termArray = newTermArray;
			length = newLength;
		}
		return count++;
	}
	
	public int staticMemorySize(){
		int size = 0;
		for (int i = 0; i < termArray.length; i++)
			if(termArray[i] != null)
				size += (termArray[i].length * 12 + termArray[i][0].array().length * 2 + 12);
		
		size += keyArrayLength * 2;
		size += bucket.length * 4;
		size += keyPos.length * 4;
		size += nextIdx.length * 4;
		
		return size;
	}
	
	public void clear(){
		Arrays.fill(bucket, -1);
		Arrays.fill(nextIdx, -1);
		count = 0;
		keyUseLength = 0;
	}
	
	//원소갯수
	public int count() {
		return count;
	}
	
}

