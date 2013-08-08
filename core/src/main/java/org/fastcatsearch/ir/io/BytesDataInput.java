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

import java.io.EOFException;
import java.io.IOException;

public class BytesDataInput extends DataInput {
	public byte[] array;
	public int offset;
	public int limit;
	public int pos;
	
	public BytesDataInput(byte[] array, int offset, int length){
		this.array = array;
		this.offset = offset;
		this.limit = Math.min(offset + length, array.length);
	}
	
	@Override
	public long position() {
		return pos - offset;
	}
	
	@Override
	public void close() throws IOException {

	}
	
	public void reset() throws IOException {
		pos = offset;
	}

	@Override
    public int read() throws IOException {
        return (pos < limit) ? (array[pos++] & 0xff) : -1;
    }
	
	@Override
	public byte readByte() throws IOException {
		if(pos >= limit){
			throw new EOFException("current pos = "+pos+", offset="+offset+", limit="+limit);
		}
		return array[pos++];
	}

	@Override
	public void readBytes(BytesBuffer dst) throws IOException {
		int len = dst.remaining();
		if((limit - pos) < len )
			throw new IOException("not enough buffer data. data limit = "+(limit - pos)+", read request = "+len);
		dst.write(array, pos, len);
		pos += len;
	}

	@Override
	public void readBytes(byte[] b, int offset, int len) throws IOException {
		if((limit - pos) < len )
			throw new IOException("not enough buffer data. data limit = "+(limit - pos)+", read request = "+len);
		
		System.arraycopy(array, pos, b, offset, len);
		pos += len;
	}

	@Override
	public long length() throws IOException {
		return limit - offset;
	}


}
