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
public class PostingBufferWithSeq {
	private static Logger logger = LoggerFactory.getLogger(PostingBufferWithSeq.class);
	
	private int count;
	private int lastDocNo;
	private BytesBuffer buffer;
	private static final int MAX_FREQ = 0xFF - 1;
	private int[] seqList = new int[8];
	private int tf;
	private int seqPos;
	private int oldSeq;
	
	public PostingBufferWithSeq(BytesBuffer buf){
		this.buffer = buf;
		count = IOUtil.readInt(buffer);
		lastDocNo = IOUtil.readInt(buffer);
		buffer.pos(buffer.length); //appendable
	}
	public PostingBufferWithSeq(){
		buffer = new BytesBuffer(32);
		this.count = 0;
		this.lastDocNo = -1;
		IOUtil.writeInt(buffer, count);
		IOUtil.writeInt(buffer, lastDocNo);
	}
	
	public void addOne(int docNo,int seq) throws IRException{
		if(docNo < lastDocNo && lastDocNo >= 0){
			throw new IRException("Input docNo cannot less than lastDocNo. docNo="+docNo+", lastDocNo="+lastDocNo);
		}
		
		if(buffer.remaining() < 10){
			byte[] newbuffer = new byte[buffer.size() * 2];
			System.arraycopy(buffer.bytes, 0, newbuffer, 0, buffer.size());
			buffer.bytes = newbuffer;
			buffer.length = buffer.bytes.length;
		}
		
		if(docNo == lastDocNo){
//			int tf = buffer.readBack();
//			if(tf >= MAX_FREQ)
//				buffer.write(MAX_FREQ);
//			else{
//				tf++;
//				buffer.write(tf);
//			}
			tf++;
			seqList[seqPos++] = seq - oldSeq	;
//			logger.debug("docNo="+docNo+" : "+tf);
		}else{
			if(count == 0){
				IOUtil.writeVariableByte(buffer, docNo);
	//			logger.debug("*** docNo="+docNo);
			}else{
				IOUtil.writeVariableByte(buffer, docNo - lastDocNo - 1);
	//			logger.debug("222 docNo="+(docNo - lastDocNo - 1));
			}
			//tf
			buffer.write(1);
			
			lastDocNo = docNo;
			count++;
		}
	}
	
	public void add(int docNo, int frq) throws IRException{
		if(docNo <= lastDocNo && lastDocNo >= 0){
			throw new IRException("Input docNo cannot less than lastDocNo. docNo="+docNo+", lastDocNo="+lastDocNo);
		}
		if(buffer.remaining() < 10){
			byte[] newbuffer = new byte[buffer.size() * 2];
			System.arraycopy(buffer.bytes, 0, newbuffer, 0, buffer.size());
			buffer.bytes = newbuffer;
			buffer.length = buffer.bytes.length;
		}
		if(count == 0){
			IOUtil.writeVariableByte(buffer, docNo);
//			logger.debug("*** docNo="+docNo);
		}else{
			IOUtil.writeVariableByte(buffer, docNo - lastDocNo - 1);
//			logger.debug("222 docNo="+(docNo - lastDocNo - 1));
		}
		//tf
		if(frq > MAX_FREQ || frq < 0)
			buffer.write(MAX_FREQ);
		else
			buffer.write(frq);
		
		lastDocNo = docNo;
		count++;
	}

	public void finish(){
		buffer.flip();
		IOUtil.writeInt(buffer, count);
		IOUtil.writeInt(buffer, lastDocNo);
//		logger.debug("%%%% lastDocNo = "+lastDocNo);
		buffer.pos(0);
	}
	
	public int size(){
		return buffer.length;
	}
	
	public int count(){
		return count;
	}
	public int lastDocNo(){
		return lastDocNo;
	}
	
	public BytesBuffer buffer(){
		return buffer;
	}
	
}
