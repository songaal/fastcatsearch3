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

import java.io.IOException;

public class BytesDataInput extends DataInput {
	public byte[] array;
	public int start;
	public int end;
	public int pos;
	
	public BytesDataInput(byte[] array, int start, int end){
		this.array = array;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public long position() {
		return pos;
	}
	
	@Override
	public void close() throws IOException {

	}
	
	public void reset() throws IOException {
		pos = start;
	}

	@Override
	public byte readByte() throws IOException {
		if(pos > end || pos < start)
			throw new ArrayIndexOutOfBoundsException("current pos = "+pos+", start="+start+", end="+end);
		return array[pos++];
	}

	@Override
	public void readBytes(BytesBuffer dst) throws IOException {
		int len = dst.remaining();
		if((end - pos) < len )
			throw new IOException("not enough buffer data. data length = "+(end - pos)+", read request = "+len);
		dst.write(array, pos, len);
		pos += len;
	}

	@Override
	public void readBytes(byte[] b, int offset, int len) throws IOException {
		System.arraycopy(array, pos, b, offset, len);
	}


}
