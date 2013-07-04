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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastByteBuffer implements ElementVector {
	private static Logger logger = LoggerFactory.getLogger(FastByteBuffer.class);
	public byte[] array;
	public int pos;
	public int limit;

	public FastByteBuffer(int size) {
		this.array = new byte[size];
		clear();
	}

	public FastByteBuffer(byte[] buffer) {
		this.array = buffer;
		clear();
	}

	public FastByteBuffer(byte[] buffer, int limit) {
		this.array = buffer;
		this.limit = limit;
	}

	public FastByteBuffer(byte[] buffer, int pos, int limit) {
		this.array = buffer;
		this.pos = pos;
		this.limit = limit;
	}

	public void init(byte[] buffer, int pos, int limit) {
		this.array = buffer;
		this.pos = pos;
		this.limit = limit;
	}

	public void clear() {
		pos = 0;
		limit = array.length;
	}

	public int size() {
		return array.length;
	}

	public FastByteBuffer flip() {
		limit = pos;
		pos = 0;
		return this;
	}

	// 현재위치로부터 offset만큼 떨어진 위치의 byte를 리턴해준다.
	// read하지 않고 값을 확인하고 싶을때 사용.
	// 2012-5-5 array[pos+offset] & 0xFF는 음수 byte를 양의 int로 바꾸어 주므로 올바른 값을 리턴할수 없다.
	// byte데이터 그대로를 리턴하도록 한다.
	public byte get(int offset) {
		return array[pos + offset];
	}

	public byte[] array() {
		return array;
	}

	public int pos() {
		return pos;
	}

	public int limit() {
		return limit;
	}

	public void pos(int pos) {
		this.pos = pos;
	}

	public void skip(int n) {
		this.pos += n;
	}

	public void limit(int limit) {
		this.limit = limit;
	}

	public int remaining() {
		return limit - pos;
	}

	public byte readByte() {
		return array[pos++];
	}

	public int read() {
		return (array[pos++] & 0xFF);
	}

	public int readBack() {
		pos--;
		return (array[pos] & 0xFF);
	}

	public void write(int b) {
		array[pos++] = (byte) b;
	}

	public void write(byte[] src) {
		System.arraycopy(src, 0, array, pos, src.length);
		pos += src.length;
	}

	public void write(byte[] src, int srcPos, int srcLen) {
		System.arraycopy(src, srcPos, array, pos, srcLen);
		pos += srcLen;
	}

	public static int compareBuffer(FastByteBuffer buf1, FastByteBuffer buf2) {
		int len1 = buf1.remaining();
		int len2 = buf2.remaining();

		int len = len1 < len2 ? len1 : len2;
		for (int i = 0; i < len; i++) {
			if (buf1.get(i) != buf2.get(i)) {
				return buf1.get(i) - buf2.get(i);
			}
		}
		return len1 - len2;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = pos; i < limit; i++) {
			sb.append(array[i]);
			sb.append(" ");
		}

		return sb.toString();
	}

	public String toAlphaString() {
		StringBuilder sb = new StringBuilder();
		for (int i = pos; i < limit; i++) {
			sb.append((char) array[i]);
		}

		return sb.toString();
	}

	@Override
	public int length() {
		return limit - pos;
	}

	@Override
	public int elementAt(int inx) {
		return array[pos + inx];
	}

	@Override
	public boolean equals(Object obj) {
		FastByteBuffer another = (FastByteBuffer) obj;
		if (remaining() != another.remaining()) {
			return false;
		}

		for (int i = 0; i < limit - pos; i++) {
			if (array[pos + i] != another.array[another.pos + i]) {
				return false;
			}
		}

		return true;
	}
}
