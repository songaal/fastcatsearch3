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

import java.io.IOException;

import org.fastcatsearch.ir.io.BytesBuffer;



public abstract class InputOutput {
	
	public abstract int readBytes(BytesBuffer dst) throws IOException;
	public abstract int readByte() throws IOException;
	
	public abstract int writeBytes(BytesBuffer dst) throws IOException;
	public abstract void writeByte(int b) throws IOException;
	
	public abstract long position() throws IOException;
	public abstract void position(long pos) throws IOException;
	public abstract void close() throws IOException;
	
	public int readBytes(byte[] dst,int offset, int length) throws IOException {
		return readBytes(new BytesBuffer(dst, offset, length));
	}
	public int writeBytes(byte[] dst,int offset, int length) throws IOException {
		return writeBytes(new BytesBuffer(dst, offset, length));
	}
	
	public long readLong() throws IOException {
		return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}
	public int readInt() throws IOException {
		return (readByte() << 24) + (readByte() << 16) + (readByte() << 8) + (readByte() << 0);
	}
	public short readShort() throws IOException {
		return (short)((readByte() << 8) + (readByte() << 0));
	}
	public char readChar() throws IOException {
		return (char)readShort();
	}
	public int readChars(char[] v, int off, int len) throws IOException {
		for(int i=off;i<len+off;i++){
			v[i] = (char)readShort();
		}
		return len;
	}
	public int readVariableByte() throws IOException {
		int v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
	
	public long readVariableByteLong() throws IOException {
		long v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = readByte();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
	
	public char[] readString() throws IOException {
		int len = readVariableByte();
		char[] cs = new char[len];
		
		for(int i=0;i<len;i++)
			cs[i] = readChar();
		return cs;
	}
	
	public void writeLong(long v) throws IOException {
		int i1 = (int)((v >>> 32) & 0xFFFFFFFF);
		int i2 = (int)(v & 0xFFFFFFFF);
		writeInt(i1);
		writeInt(i2);
	}
	public void writeInt(int v) throws IOException {
		writeByte((v >>> 24) & 0xFF);
		writeByte((v >>> 16) & 0xFF);
		writeByte((v >>> 8) & 0xFF);
		writeByte((v >>> 0) & 0xFF);
	}
	public void writeShort(int v) throws IOException {
		writeByte((v >>> 8) & 0xFF);
		writeByte((v >>> 0) & 0xFF);
	}
	public void writeChar(int v) throws IOException {
		writeShort( v);
	}
	public int writeVariableByte(int v) throws IOException {
		int byteCnt=0;
		do{
			int b = (byte)(v & 0x7F); //하위 7비트 
			v >>>= 7; //오른쪽으로 7비트 shift
			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
				b |= 0x80;
				writeByte(b);
			}else{
				writeByte(b);
			}
			byteCnt++;
		} while (v != 0);
		
		return byteCnt;
	}
	
	public int writeChars(char[] v) throws IOException {
		int wroteByte = 0;
		wroteByte += writeVariableByte( v.length);
		for (int i = 0; i < v.length; i++) {
			writeChar( v[i]);
			wroteByte += 2;
		}
		
		return wroteByte;
	}
	public int writeChars(char[] v, int start, int length) throws IOException {
		int wroteByte = 0;
		wroteByte += writeVariableByte(length);
		for (int i = start; i < length; i++) {
			writeChar(v[i]);
			wroteByte += 2;
		}
		
		return wroteByte;
	}
	
	public int writeString(String v) throws IOException {
		int wroteByte = 0;
		wroteByte += writeVariableByte( v.length());
		for (int i = 0; i < v.length(); i++) {
			writeChar( v.charAt(i));
			wroteByte += 2;
		}
		
		return wroteByte;
	}
	//총 length의 고정길이를 쓴다. 남는 공간은 0으로 채운다.
	public int writeString(String v, int length) throws IOException {
		int wroteByte = 0;
		int strLen = (v.length() < length) ? v.length() : length;
		
		wroteByte += writeVariableByte( length);
		for (int i = 0; i < strLen; i++) {
			writeChar( v.charAt(i));
			wroteByte += 2;
		}
		//문자열 길이가 length보다 클경우 이 루프는 돌지 않는다.
		for (int i = strLen; i < length; i++) {
			writeChar( 0);
			wroteByte += 2;
		}
		
		return wroteByte;
	}
}
