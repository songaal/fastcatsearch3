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

import org.apache.lucene.util.BytesRef;

public class BytesDataOutput extends DataOutput {
	protected byte[] buf;
	protected int pos;

	public byte[] array() {
		return buf;
	}

	public BytesDataOutput() {
		this(32);
	}

	public BytesDataOutput(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative initial size: " + size);
		}
		buf = new byte[size];
	}

	public BytesRef bytesRef() {
		return new BytesRef(buf, 0, pos);
	}

	public int length(){
		return buf.length;
	}
	@Override
	public void close() throws IOException {

	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public long position() throws IOException {
		return pos;
	}

	@Override
	public void seek(long p) throws IOException {
		pos = (int) p;
	}

	@Override
	public void writeByte(byte b) throws IOException {
		int newPos = pos + 1;
		if (newPos > buf.length) {
			byte newbuf[] = new byte[Math.max(buf.length << 1, newPos)];
			System.arraycopy(buf, 0, newbuf, 0, pos);
			buf = newbuf;
		}
		buf[pos] = (byte) b;
		pos = newPos;
	}

	@Override
	public void writeBytes(byte[] dst, int offset, int length) throws IOException {
		if ((offset < 0) || (offset > dst.length) || (length < 0) || ((offset + length) > dst.length) || ((offset + length) < 0)) {
			throw new IndexOutOfBoundsException("offset="+offset+", length="+length+", dst.length="+dst.length);
		} else if (length == 0) {
			return;
		}

		int newPos = pos + length;
		if (newPos > buf.length) {
			byte newbuf[] = new byte[Math.max(buf.length << 1, newPos)];
			System.arraycopy(buf, 0, newbuf, 0, pos);
			buf = newbuf;
		}
		System.arraycopy(dst, offset, buf, pos, length);
		pos = newPos;

	}

	public void reset() {
		pos = 0;
	}

}
