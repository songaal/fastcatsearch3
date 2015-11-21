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


/**
 * 
 * @author sangwook
 *
 */
public class PostingBufferWithPosition extends PostingBuffer {
	
	private BytesBuffer positionBuffer;
	private int lastDocPosition;
	
	public PostingBufferWithPosition(){
		super();
		positionBuffer = new BytesBuffer(32);
	}
	
	@Override
	public void addOne(int docNo, int position) throws IRException{
		//super.addOne 에서 lastDocNo가 변경되므로 먼저 백업.
		int lastDocNo = this.lastDocNo;
		
		super.addOne(docNo, position);
		
		if(positionBuffer.remaining() < 5){ //vb는 최대 5바이트기록가능하다.
			byte[] newbuffer = new byte[positionBuffer.size() * 2];
			System.arraycopy(positionBuffer.bytes, 0, newbuffer, 0, positionBuffer.pos());
			positionBuffer.bytes = newbuffer;
			positionBuffer.limit = newbuffer.length;
		}
		
		//동일한 문서번호면 freq를 올려준다.
		if(docNo == lastDocNo){
			IOUtil.writeVInt(positionBuffer, position - lastDocPosition - 1);
		}else if(docNo > lastDocNo){
			IOUtil.writeVInt(positionBuffer, position);
		}
		lastDocPosition = position;
	}
	
	@Override
	protected void writeLastDocInfo(){
		super.writeLastDocInfo();
		
		//positionVector 기록.
		positionBuffer.flip();
		
		ensurePostingVectorCapasity(positionBuffer.length());
		
		postingVector.write(positionBuffer.array(), positionBuffer.pos(), positionBuffer.length());
		//positionVector 초기화.
		if(positionBuffer.size() >= 128){
			//너무크면 버리고 새로 만든다.
			positionBuffer = new BytesBuffer(32);
		}else{
			positionBuffer.clear();
		}
		
	}

	@Override
	public int size(){
		return postingVector.limit() + positionBuffer.limit();
	}
	
}
