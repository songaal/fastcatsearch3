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

import org.fastcatsearch.al.HashFunctions;
import org.fastcatsearch.ir.common.IRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 대용량의 hashset 처리를 위해서 파일기반으로 hashset를 처리하는 클래스.
 *
 * */
public class FileBaseHashSet {
	private static final Logger logger = LoggerFactory.getLogger(FileBaseHashSet.class);

	private static final HashFunctions hfunc = HashFunctions.RSHash;

    private File f;

	private int[] bucket;
	private char[] keyArray;
	private int[] keyPos;
	private int[] nextIdx;

	private int bucketSize;
	private int length;
	private int count;
	private int keyArrayLength;
	private int keyUseLength;

    private RandomAccessFile raf;

    private long countOffset;
    private long keyPosOffset;
    private long nextOffset;
    private long bucketOffset;
    private long keyArrayOffset;

    /**
     * 기존파일 오픈
     * */
    public FileBaseHashSet(File f) throws FileNotFoundException {
        raf = new RandomAccessFile(f, "rw");
        load();
    }

    /**
     * 새로 생성
     * */
	public FileBaseHashSet(File f, int bucketSize){
        if(f.exists()) {
            f.delete();
        }

        try {
            f.createNewFile();
            raf = new RandomAccessFile(f, "rw");
        } catch (IOException e) {
            logger.error("", e);
        }
        init(bucketSize);
	}

    public void init(int bucketSize){
        this.bucketSize = bucketSize;
        length = bucketSize;
        count = 0;
        keyArrayLength = bucketSize * 5;
        keyUseLength = 0;

        bucket = new int[bucketSize];
        keyArray = new char[keyArrayLength];
        keyPos = new int[length];
        nextIdx = new int[length];

        Arrays.fill(bucket, -1);
    }

    public void load() {

    }

//	public FileBaseHashSet(File file) throws IRException{
//		this.file = file;
//		try {
//			long st = System.currentTimeMillis();
//
//			IndexInput input = new BufferedFileInput(file);
//			bucketSize = input.readInt();
//			count = input.readInt();
//			keyUseLength = input.readInt();
//
//			bucket = new int[bucketSize];
//			keyArray = new char[keyUseLength];
//			keyPos = new int[count];
//			nextIdx = new int[count];
//
//			for (int i = 0; i < bucketSize; i++) {
//				bucket[i] = input.readInt();
//			}
//			for (int i = 0; i < keyUseLength; i++) {
//				keyArray[i] = input.readUChar();
//			}
//			for (int i = 0; i < count; i++) {
//				keyPos[i] = input.readInt();
//			}
//			for (int i = 0; i < count; i++) {
//				nextIdx[i] = input.readInt();
//			}
//			input.close();
//
//			logger.info("Load dictionary done! {}, entry = {}, time = {}ms", new Object[]{file.getAbsolutePath(), count, (System.currentTimeMillis() - st)});
//		} catch (IOException e) {
//			logger.error("IOException",e);
//			throw new IRException(e);
//		}
//
//	}
	
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
			
			output.close();

//			logger.info("Wrote {}, {}", Formatter.getFormatSize(output.size()), file.getAbsolutePath());			
			logger.info("Wrote {}, {}", file.length(), file.getAbsolutePath());
		} catch (IOException e) {
			logger.error("IOException",e);
			throw new IRException(e);
		}
	}

	private boolean isTheSame(CharVector term, int id) {
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
	
	public boolean put(CharVector term) {
//		logger.debug("term >> {}", term);
		int hashValue = hfunc.hash(term, bucketSize);
		
		int prev = -1;
		int idx = bucket[hashValue];

		while(idx >= 0){
			if(isTheSame(term, idx))
				break;
			
			prev = idx;
			idx = nextIdx[idx];
		}
//		logger.debug("idx = "+idx);
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
//			logger.debug("NextIdx = "+idx+" , term.length="+term.length);
			if (keyUseLength + term.length() >= keyArrayLength) {
				keyArrayLength *= 1.2;
				char[] newArray = new char[keyArrayLength];
				System.arraycopy(keyArray, 0, newArray, 0, keyUseLength);
				keyArray = newArray;
			}
			keyPos[idx] = keyUseLength;
			
			for (int i=0; i < term.length(); i++) {
				keyArray[keyUseLength++] = term.charAt(i);
//				logger.debug("t = "+keyArray[keyUseLength - 1]);
			}
			
			nextIdx[idx] = -1;
			if(prev != -1)
				nextIdx[prev] = idx;
			else
				bucket[hashValue] = idx;
			
		}
		
		return true;
	}
	
	public boolean contains(CharVector term) {
		int hashValue = hfunc.hash(term, bucketSize);
		int idx = bucket[hashValue];
		
//		logger.debug(term+" = "+hashValue+", idx="+idx);
		while(idx >= 0){
			if(isTheSame(term, idx))
				break;
			
			idx = nextIdx[idx];
		}
		
		if(idx < 0)
			return false; //검색실패 
		else{
			return true;
		}
	}
	
	private int getNextIdx() {
		if(count >= length){
			int newLength = (int) (length * 1.2);
//			logger.debug("Grow length = "+length+" => "+newLength+", new int * 2, new PostingBuffer[], arraycopy * 3");
			int[] newKeyPos = new int[newLength];
			int[] newNext = new int[newLength];
			
			System.arraycopy(keyPos, 0, newKeyPos, 0, count);
			System.arraycopy(nextIdx, 0, newNext, 0, count);
			
			keyPos = newKeyPos;
			nextIdx = newNext;
			length = newLength;
		}
		return count++;
	}
	
	public int staticMemorySize(){
		int size = 0;
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
	
	//entry count
	public int count() {
		return count;
	}
	
}
