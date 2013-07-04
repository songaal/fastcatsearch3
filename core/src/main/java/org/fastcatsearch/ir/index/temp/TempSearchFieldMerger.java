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

package org.fastcatsearch.ir.index.temp;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.index.IndexFieldOption;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.ByteArrayOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.FastByteBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class TempSearchFieldMerger {
	protected static Logger logger = LoggerFactory.getLogger(TempSearchFieldMerger.class);
	
	protected int[] heap;
	protected TempSearchFieldReader[] reader;
	protected int flushCount;
	
	public TempSearchFieldMerger(int flushCount, long[] flushPosition, File tempFile) throws IOException{ 
		this.flushCount = flushCount;
		reader = new TempSearchFieldReader[flushCount];
		for (int m = 0; m < flushCount; m++) {
			reader[m] = new TempSearchFieldReader(m, tempFile, flushPosition[m]);
			reader[m].next();
		}
	}
	
	protected void prepareNextSearchField(int i) throws IOException{
		if(i <= 0)
			return; 
			
		for (int m = 0; m < flushCount; m++) {
//			logger.debug("left()-"+m+" = "+reader[m].left());
			boolean isResume = reader[m].resume();
			reader[m].next();
		}
	
	}
	
	public void mergeAndMakeIndex(File baseDir, int indexInterval, IndexFieldOption[] fieldIndexOptions) throws IOException{
		logger.debug("**** mergeAndMakeIndex ****");
		logger.debug("flushCount={}", flushCount);
		
		if(flushCount <= 0){
			return;
		}
		
		Output postingOutput = new BufferedFileOutput(IRFileName.getRevisionDir(baseDir, 0), IRFileName.postingFile, false);
	    Output lexiconOutput = new BufferedFileOutput(IRFileName.getRevisionDir(baseDir, 0), IRFileName.lexiconFile, false);
	    Output indexOutput = new BufferedFileOutput(IRFileName.getRevisionDir(baseDir, 0), IRFileName.indexFile, false);
	    ByteArrayOutput tempPostingOutput = new ByteArrayOutput(1024 * 1024);
		CharVector cv = null;
		CharVector cvOld = null;

		try{
			int fieldCount = fieldIndexOptions.length;
			postingOutput.writeInt(fieldCount);
			for (int i = 0; i < fieldCount; i++) {
				postingOutput.writeInt(fieldIndexOptions[i].value());
			}
			
			//to each field
			for (int i = 0; i < fieldCount; i++) {
				logger.debug("## MERGE field = {}", i);
	
				prepareNextSearchField(i);
				makeHeap(flushCount);
				
				//동일한 단어는 최대 flush갯수 만큼 buffer 배열에 쌓이게 된다.
				FastByteBuffer[] buffers = new FastByteBuffer[flushCount] ;
				int bufferCount = 0; //posting buffer's count in the same term
				
				int termCount = 0;
				int indexTermCount = 0;
				
				long lexiconFileHeadPos = lexiconOutput.position();
				long indexFileHeadPos = indexOutput.position();
				
				lexiconOutput.writeInt(termCount);//termCount
				indexOutput.writeInt(indexTermCount);//indexTermCount
//				boolean isStorePosition = fieldIndexOptions[i].isStorePosition();

				while(true){
					int idx = heap[1];
					cv = reader[idx].term();
					//if cv == null, all readers are done
					//if cvOld !=  cv , term is changed and cvOld has to be wrote 
					if( (cv == null  || !cv.equals(cvOld)) && cvOld!= null){
						//merge buffers
						int prevDocNo = -1;
						int totalCount = 0;
						
//						logger.debug("MERGE {}, count={}", cvOld, bufferCount);
						for (int k = 0; k < bufferCount; k++) {
							FastByteBuffer buf = buffers[k];
							buf.flip();
							
							int count = IOUtil.readInt(buf);
							int lastDocNo = IOUtil.readInt(buf);
							totalCount += count;
							
							if(k == 0){
								tempPostingOutput.writeBytes(buf.array(), buf.pos(), buf.remaining());
							}else{
								int firstNo = IOUtil.readVariableByte(buf);
								int newDocNo = firstNo - prevDocNo - 1;
								
								IOUtil.writeVariableByte(tempPostingOutput, newDocNo);
								tempPostingOutput.writeBytes(buf.array(), buf.pos(), buf.remaining());
							}
							
							prevDocNo = lastDocNo;
						}
						
						long postingPosition = postingOutput.position();
						//write size
						postingOutput.writeVariableByte(IOUtil.SIZE_OF_INT * 2 + (int)tempPostingOutput.position());
						//count, lastDocNo are required for later (index compact)
						postingOutput.writeInt(totalCount);//count
						postingOutput.writeInt(prevDocNo);//lastDocNo
						postingOutput.writeBytes(tempPostingOutput.array(), 0, (int)tempPostingOutput.position());
						tempPostingOutput.reset();
						
						bufferCount = 0;
						
						long pointer = lexiconOutput.position();
	//					write term
						lexiconOutput.writeUString(cvOld.array, cvOld.start, cvOld.length);
						lexiconOutput.writeLong(postingPosition);
						
						if(indexInterval > 0 && (termCount % indexInterval) == 0){
							indexOutput.writeUString(cvOld.array, cvOld.start, cvOld.length);
							indexOutput.writeLong(pointer);
							indexOutput.writeLong(postingPosition);
							indexTermCount++;
						}
						
						termCount++;
					}
					
					try{
						buffers[bufferCount++] = reader[idx].buffer();
					}catch(ArrayIndexOutOfBoundsException e){
						logger.info("bufferCount = {}, buffers.len = {}, idx = {}, reader = {}", new Object[]{bufferCount, buffers.length, idx, reader.length});
						throw e;
					}
					//backup cv to old
					cvOld = cv;
					
					reader[idx].next();
					
					if(cv == null){
						//all readers are done
						break;
					}
					
					heapify(1, flushCount);
					
				} //while(true)
			
				//Write term count on head position
				long prevPos = lexiconOutput.position();
				lexiconOutput.position(lexiconFileHeadPos);
				lexiconOutput.writeInt(termCount);
				lexiconOutput.position(prevPos);
				logger.debug("termCount = {}, indexTermCount = {}, indexInterval={},{}", termCount , indexTermCount, indexInterval, (termCount % indexInterval));
				if(termCount > 0){
					indexOutput.flush();
	//				logger.debug("*** indexOutput.size() = "+indexOutput.size());
					prevPos = indexOutput.position();
					indexOutput.position(indexFileHeadPos);
					indexOutput.writeInt(indexTermCount);
					indexOutput.position(prevPos);
					indexOutput.flush();
	//				logger.debug("*** indexOutput.size() = "+indexOutput.size());
				}else{
					//이미 indexTermCount는 0으로 셋팅되어 있으므로 기록할 필요없음.
					long pointer = lexiconOutput.position();
					indexOutput.writeLong(pointer);
				}
				logger.debug("## write index term = {} at {}", indexTermCount, indexFileHeadPos);
			}
			indexOutput.flush();
			
		}finally {
			IOException exception = null;
			
			try{
				if(postingOutput != null){
					postingOutput.close();
				}
			}catch(IOException e){ 
				exception = e;
			}
			try{
				if(lexiconOutput != null){
					lexiconOutput.close();
				}
			}catch(IOException e){ 
				exception = e;
			}
			try{
				if(indexOutput != null){
					indexOutput.close();
				}
			}catch(IOException e){ 
				exception = e;
			}
			
			if(exception != null){
				throw exception;
			}
		}
	}
	

//	private void printPosting(FastByteBuffer buf, boolean isStorePosition) {
//		
//		FastByteBuffer buffer = new FastByteBuffer(buf.array, buf.pos, buf.limit);
//		
//		int count = IOUtil.readInt(buffer);
//		int lastDocNo = IOUtil.readInt(buffer);
//		logger.debug("검증 printPosting count[{}] lastDoc[{}] data={}", count, lastDocNo, buf);
//		for (int i = 0; i < count; i++) {
//			int docNo = IOUtil.readVariableByte(buffer);
//			int tf = IOUtil.readVariableByte(buffer);
//			
//			logger.debug("printPosting {}:{} remain={}", docNo, tf, buffer.remaining());
//			if(isStorePosition){
//				for (int j = 0; j < tf; j++) {
//					int pos = IOUtil.readVariableByte(buffer);
//					
//					logger.debug("printPosting pos [{}] {}", j, pos);
//				}
//			}
//		}
//		
//	}

	public void close() throws IOException{
		IOException exception = null;
		for (int i = 0; i < flushCount; i++) {
			if(reader[i] != null){
				try{
					reader[i].close();
				}catch(IOException e){ 
					exception = e;
				}
			}
		}
		if(exception != null){
			throw exception;
		}
	}
	
	protected void makeHeap(int heapSize){
		heap = new int[heapSize + 1];
		//index starts from 1
		for (int i = 0; i < heapSize; i++) {
			heap[i+1] = i;
		}
		
		int n = heapSize >> 1; //last inner node index
		
		for (int i = n; i > 0; i--) {
			heapify(i, heapSize);
		}
		
	}
	
	protected void heapify(int idx, int heapSize){
		
		int temp = -1;
		int child = -1;

		while(idx <= heapSize){
			int left = idx << 1;// *=2
			int right = left + 1;
			
			if(left <= heapSize){
				if(right <= heapSize){
					//키워드가 동일할 경우 먼저 flush된 reader가 우선해야, docNo가 오름차순 정렬순서대로 올바로 기록됨.
					//flush후 머징시 문제가 생기는 버그 해결됨 2013-5-21 swsong
					int c = compareKey(left, right);
					if(c < 0){
						child = left;
					}else if(c > 0){
						child = right;
					}else{
						//하위 value 둘이 같아서 seq확인.
						//같다면 id가 작은게 우선.
						int a = heap[left];
						int b = heap[right];
						if(reader[a].getId() < reader[b].getId()){
							child = left;
						}else{
							child = right;
						}
					}
				}else{
					//if there is no right el.
					child = left;
				}
			}else{
				//no children
				break;
			}
			
			//compare and swap
			int c = compareKey(child, idx);
			if(c < 0){
				temp = heap[child];
				heap[child] = heap[idx];
				heap[idx] = temp;
				idx = child;
//				System.out.println("idx1="+idx);
			}else if(c == 0){
				//하위와 자신의 value가 같아서 seq확인
				//같다면 seq가 작은게 우선.
				int a = heap[idx];
				int b = heap[child];
				if(reader[a].getId() > reader[b].getId()){
					//하위의 seq가 작아서 child채택!
					temp = heap[child];
					heap[child] = heap[idx];
					heap[idx] = temp;
					idx = child;
				}else{
					//내것을 그대로 사용.
					//sorted
					break;
				}
			}else{
				//sorted, then do not check child
				break;
			}

		}
	}
	
	protected int compareKey(int one, int another){

		int a = heap[one];
		int b = heap[another];
		
		return compareKey(reader[a].term(), reader[b].term());
	}
	
	protected int compareKey(CharVector term1, CharVector term2){
		
		//reader gets EOS, returns null
		if(term1 == null && term2 == null){
			return 0;
		}else if(term1 == null)
			return 1;
		else if(term2 == null)
			return -1;
		
		int len = (term1.length < term2.length) ? term1.length : term2.length;
				
		int aoff = term1.start;
		int boff = term2.start;
		
		for (int i = 0; i < len; i++) {
			if(term1.array[aoff + i] != term2.array[boff + i])
				return term1.array[aoff + i] - term2.array[boff + i];
		}
		
		if(term1.length != term2.length)
			return term1.length - term2.length;
		
		return term1.length - term2.length;
	}
}
