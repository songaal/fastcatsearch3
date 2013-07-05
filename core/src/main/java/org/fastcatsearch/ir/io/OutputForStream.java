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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputForStream extends Output {

	private OutputStream os;
	
	public OutputForStream(OutputStream os){
		this.os = new BufferedOutputStream(os);
	}
	@Override
	public void writeBytes(BytesBuffer dst) throws IOException {
		int n = dst.remaining();
		os.write(dst.array, dst.offset, dst.remaining());
		dst.pos(dst.offset + n);
	}

	@Override
	public void writeByte(int b) throws IOException {
		os.write(b);
	}

	@Override
	public long position() throws IOException {
		throw new IOException("지원되지 않는 오퍼레이션입니다."); 
	}

	@Override
	public void position(long p) throws IOException {
		throw new IOException("지원되지 않는 오퍼레이션입니다."); 
	}

	@Override
	public void close() throws IOException {
		os.flush();
		os.close();
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void writeBytes(byte[] dst, int offset, int length)
			throws IOException {
		os.write(dst, offset, length);

	}

	@Override
	public long size() throws IOException {
		throw new IOException("지원되지 않는 오퍼레이션입니다."); 
	}

	@Override
	public void setLength(long newLength) throws IOException {
		throw new IOException("지원되지 않는 오퍼레이션입니다."); 
	}

}
