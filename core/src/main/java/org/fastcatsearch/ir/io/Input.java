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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Input implements Cloneable {
	protected static Logger logger = LoggerFactory.getLogger(Input.class);

	public abstract int readBytes(FastByteBuffer dst) throws IOException;

	public abstract int readByte() throws IOException;

	public abstract long position() throws IOException;

	public abstract void position(long p) throws IOException;

	public abstract void close() throws IOException;

	public abstract long size() throws IOException;

	@Override
	public Input clone() {
		try {
			return (Input) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("This cannot happen: Failing to clone DataInput");
		}
	}

	public boolean readBoolean() throws IOException {
		if (readByte() == 1) {
			return true;
		} else {
			return false;
		}
	}

	public int readBytes(byte[] dst, int offset, int length) throws IOException {
		return readBytes(new FastByteBuffer(dst, offset, offset + length));
	}

	public int readComplementBytes(byte[] dst, int offset, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			dst[offset + i] = (byte) (0xFF - readByte());
		}
		return length;
	}

	public int readComplementByte() throws IOException {
		return 0xFF - readByte();
	}

	public long readLong() throws IOException {
		return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}

	public int readInt() throws IOException {
		return (readByte() << 24) + (readByte() << 16) + (readByte() << 8) + (readByte() << 0);
	}

	public short readShort() throws IOException {
		return (short) ((readByte() << 8) + (readByte() << 0));
	}

	public char readUChar() throws IOException {
		return (char) readShort();
	}

	public char readAChar() throws IOException {
		return (char) readByte();
	}

	public int readVariableByte() throws IOException {
		int v = 0;
		int b = 0;
		int shift = 0;
		do {
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while ((b & 0x80) > 0);

		return v;
	}

	public long readVariableByteLong() throws IOException {
		long v = 0;
		int b = 0;
		int shift = 0;
		do {
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while ((b & 0x80) > 0);

		return v;
	}

	public char[] readAString() throws IOException {
		int len = readVariableByte();
		char[] cs = new char[len];

		for (int i = 0; i < len; i++)
			try {
				cs[i] = readAChar();
			} catch (EOFException e) {
				logger.debug("EOFException i= " + i + ", cs.len=" + cs.length);
				throw e;
			}
		return cs;
	}

	public char[] readUString() throws IOException {
		int len = readVariableByte();
		char[] cs = new char[len];

		for (int i = 0; i < len; i++)
			try {
				cs[i] = readUChar();
			} catch (EOFException e) {
				logger.debug("EOFException i= " + i + ", cs.len=" + cs.length);
				throw e;
			}
		return cs;
	}

	public int readUString(char[] cs, int maxLength) throws IOException {
		int len = readVariableByte();
		if (maxLength < len)
			return len;

		for (int i = 0; i < len; i++) {
			try {
				cs[i] = readUChar();
			} catch (EOFException e) {
				logger.debug("EOFException i= " + i + ", cs.len=" + cs.length);
				throw e;
			} catch (ArrayIndexOutOfBoundsException e) {
				throw e;
			}
		}

		return len;
	}

	public char[] readAChars(int len) throws IOException {
		char[] cs = new char[len];

		for (int i = 0; i < len; i++)
			try {
				cs[i] = readAChar();
			} catch (EOFException e) {
				logger.debug("EOFException i= " + i + ", cs.len=" + cs.length);
				throw e;
			}
		return cs;
	}

	public char[] readUChars(int len) throws IOException {
		char[] cs = new char[len];

		for (int i = 0; i < len; i++)
			try {
				cs[i] = readUChar();
			} catch (EOFException e) {
				logger.debug("EOFException i= " + i + ", cs.len=" + cs.length);
				throw e;
			}
		return cs;
	}

	public void readUChars(char[] cs, int len) throws IOException {
		for (int i = 0; i < len; i++)
			try {
				cs[i] = readUChar();
			} catch (EOFException e) {
				logger.debug("EOFException i= " + i + ", cs.len=" + cs.length);
				throw e;
			}
	}

}
