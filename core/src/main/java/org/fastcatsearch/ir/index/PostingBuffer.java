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

package org.fastcatsearch.ir.index;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author sangwook
 *
 */
public class PostingBuffer {
	protected static Logger logger = LoggerFactory.getLogger(PostingBuffer.class);
	
	protected int postingSize;
	protected int lastDocNo;
	protected BytesBuffer postingVector;
	protected int lastDocDelta;
	protected int lastDocFrequency;
	
	public PostingBuffer(){
		postingVector = new BytesBuffer(32);
		this.postingSize = 0;
		this.lastDocNo = -1;
		IOUtil.writeInt(postingVector, postingSize);
		IOUtil.writeInt(postingVector, lastDocNo);
	}
	
	
	protected void ensurePostingVectorCapasity(int additionalSize){
		if(postingVector.remaining() < additionalSize){
			//2^n 승으로 증가.
			int newAdditionalSize = postingVector.size() << 1;
			
			while(newAdditionalSize < postingVector.pos() + additionalSize){
				newAdditionalSize <<= 1;
			}
		
			byte[] newbuffer = new byte[newAdditionalSize];
			System.arraycopy(postingVector.bytes, 0, newbuffer, 0, postingVector.pos());
			postingVector.bytes = newbuffer;
			postingVector.limit = newbuffer.length;
		}
	}
	public void addOne(int docNo, int position) throws IRException{
//		logger.debug("Posting add >> {}, {}", docNo, position);
		//동일한 문서번호면 freq를 올려준다.
		if(docNo == lastDocNo){
			lastDocFrequency++;
		}else if(docNo > lastDocNo){
			//문서번호가 증가하면 기록.
			if(postingSize == 0){
				//첫 문서면 기록하지 않고 두번째문서부터만 이전 문서기록.
				lastDocDelta = docNo;
			}else{
				writeLastDocInfo();
				lastDocDelta = docNo - lastDocNo - 1;
			}
			lastDocNo = docNo;
			lastDocFrequency++;
			postingSize++;
		}else{
			throw new IRException("Input docNo cannot less than lastDocNo. docNo="+docNo+", lastDocNo="+lastDocNo);
		}
	}
	
	protected void writeLastDocInfo(){
		ensurePostingVectorCapasity(10);
		//lastDocDelta 기록.
		IOUtil.writeVInt(postingVector, lastDocDelta);
		//lastDocFrequency 기록.
		IOUtil.writeVInt(postingVector, lastDocFrequency);
		lastDocFrequency = 0;
	}

	public void finish(){
		//flush 안된 남은 posting정보 기록. 
		if(lastDocFrequency > 0){
			writeLastDocInfo();
		}
		postingVector.flip();
		IOUtil.writeInt(postingVector, postingSize);
		IOUtil.writeInt(postingVector, lastDocNo);
		postingVector.pos(0);
	}
	
	public int size(){
		return postingVector.limit();
	}
	
	public int count(){
		return postingSize;
	}
	public int lastDocNo(){
		return lastDocNo;
	}
	public int firstDocNo(){
		return IOUtil.readVInt(postingVector.bytes, 8);
	}
	
	public BytesBuffer buffer(){
		return postingVector;
	}
	
}
