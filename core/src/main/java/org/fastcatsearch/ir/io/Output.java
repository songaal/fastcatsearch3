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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Output {
	private static Logger logger = LoggerFactory.getLogger(Output.class);
	public abstract void writeBytes(FastByteBuffer dst) throws IOException;
	public abstract void writeByte(int b) throws IOException;
	public abstract long position() throws IOException;
	public abstract void position(long p) throws IOException;
	public abstract void close() throws IOException;
	public abstract void flush() throws IOException;
	public abstract void writeBytes(byte[] dst,int offset, int length) throws IOException;
	public abstract long size() throws IOException;
	public abstract void setLength(long newLength) throws IOException;
	
	public void writeBytes(byte[] dst) throws IOException{
		writeBytes(dst, 0, dst.length);
	}
	
	public void writeBoolean(boolean b) throws IOException {
		if(b){
			writeByte(1);
		}else{
			writeByte(0);
		}
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
	public void writeAChar(int v) throws IOException {
		if(v > 0xFF)
			throw new IOException("Input number exceeds byte range. char="+(char)v);
		writeByte(v);
	}
	public void writeUChar(int v) throws IOException {
		writeShort(v);
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
	
//	public int writeChars(char[] v) throws IOException {
//		int wroteByte = 0;
//		wroteByte += writeVariableByte( v.length);
//		for (int i = 0; i < v.length; i++) {
//			writeUChar(v[i]);
//			wroteByte += 2;
//		}
//		
//		return wroteByte;
//	}
	
	//String와 chars의 차이는 내부적으로 문자길이를 vb write하고 안하고의 차이이다. 
	public int writeAString(char[] v, int start, int length) throws IOException {
		int wroteByte = 0;
		wroteByte += writeVariableByte(length);
		for (int i = 0; i < length; i++) {
			writeAChar(v[start + i]);
			wroteByte ++;
		}
		
		return wroteByte;
	}
	public int writeUString(char[] v, int start, int length) throws IOException {
		int wroteByte = 0;
		wroteByte += writeVariableByte(length);
		for (int i = 0; i < length; i++) {
			writeUChar(v[start + i]);
			wroteByte += 2;
		}
		
		return wroteByte;
	}
	//writeXChars function doesn't write character's length.
	public int writeAChars(char[] v, int start, int length) throws IOException {
		int wroteByte = 0;
		for (int i = 0; i < length; i++) {
			writeAChar(v[start + i]);
			wroteByte ++;
		}
		
		return wroteByte;
	}
	public int writeUChars(char[] v, int start, int length) throws IOException {
		int wroteByte = 0;
		for (int i = 0; i < length; i++) {
			writeUChar(v[start + i]);
			wroteByte += 2;
		}
		
		return wroteByte;
	}
	
//	public int writeString(String v) throws IOException {
//		int wroteByte = 0;
//		wroteByte += writeVariableByte( v.length());
//		for (int i = 0; i < v.length(); i++) {
//			writeUChar( v.charAt(i));
//			wroteByte += 2;
//		}
//		
//		return wroteByte;
//	}
//	//총 length의 고정길이를 쓴다. 남는 공간은 0으로 채운다.
//	public int writeString(String v, int length) throws IOException {
//		int wroteByte = 0;
//		int strLen = (v.length() < length) ? v.length() : length;
//		
//		wroteByte += writeVariableByte(length);
//		for (int i = 0; i < strLen; i++) {
//			writeUChar(v.charAt(i));
//			wroteByte += 2;
//		}
//		//문자열 길이가 length보다 클경우 이 루프는 돌지 않는다.
//		for (int i = strLen; i < length; i++) {
//			writeUChar(0);
//			wroteByte += 2;
//		}
//		
//		return wroteByte;
//	}

}
