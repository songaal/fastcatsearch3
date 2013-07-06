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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public final class IOUtil {
	public static int PAGESIZE = 1 * 1024;
	public static int FILEBLOCKSIZE = 4 * 1024;
	public static int SIZE_OF_SHORT = Short.SIZE / 8;
	public static int SIZE_OF_INT = Integer.SIZE / 8;
	public static int SIZE_OF_LONG = Long.SIZE / 8;
	public static int BITS_OF_BYTE = Byte.SIZE;
	public static int BITS_OF_LONG = Long.SIZE;
	public static int UNSIGNED_SHORT_MAX_VALUE = Short.MAX_VALUE * 2 + 1;
	
	public static void write(BytesBuffer buffer, byte[] array, int offset, int len) {
		System.arraycopy(array, offset, buffer.bytes, buffer.offset, len);
		buffer.offset += len;
	}
	
	public static int writeLong(BytesBuffer buffer, long v) {
		int i1 = (int)((v >>> 32) & 0xFFFFFFFF);
		int i2 = (int)(v & 0xFFFFFFFF);
		writeInt(buffer,i1);
		writeInt(buffer,i2);
		return 8;
	}
	public static int writeInt(byte[] buffer, int offset, int v) {
		buffer[offset + 0] = (byte) ((v >>> 24) & 0xFF);
		buffer[offset + 1] = (byte) ((v >>> 16) & 0xFF);
		buffer[offset + 2] = (byte) ((v >>> 8) & 0xFF);
		buffer[offset + 3] = (byte) ((v >>> 0) & 0xFF);
		return 4;
	}
	public static int writeInt(BytesBuffer buffer, int v) {
		buffer.write((v >>> 24) & 0xFF);
		buffer.write((v >>> 16) & 0xFF);
		buffer.write((v >>> 8) & 0xFF);
		buffer.write((v >>> 0) & 0xFF);
		return 4;
	}
	public static int writeInt(OutputStream out, int v) throws IOException {
		out.write((v >>> 24) & 0xFF);
		out.write((v >>> 16) & 0xFF);
		out.write((v >>> 8) & 0xFF);
		out.write((v >>> 0) & 0xFF);
		return 4;
	}
//	public static int writeInt(Output output, int v) throws IOException {
//		output.writeByte((v >>> 24) & 0xFF);
//		output.writeByte((v >>> 16) & 0xFF);
//		output.writeByte((v >>> 8) & 0xFF);
//		output.writeByte((v >>> 0) & 0xFF);
//		return 4;
//	}
	public static int writeShort(BytesBuffer buffer, int v) {
		buffer.write((v >>> 8) & 0xFF);
		buffer.write((v >>> 0) & 0xFF);
		return 2;
	}

	public static int writeShort(byte[] buffer, int pos, int v) {
		buffer[pos] = (byte) ((v >>> 8) & 0xFF);
		buffer[pos+1] = (byte) ((v >>> 0) & 0xFF);
		return 2;
	}
	
	public static void writeUChar(BytesBuffer buffer, int v) {
		writeShort(buffer, v);
	}

	public static void writeAChar(BytesBuffer buffer, int v) {
		buffer.write(v & 0xFF);
	}

	public static int writeVariableByte(BytesBuffer buffer, int v) {
		int byteCnt=0;
		do{
			int b = (byte)(v & 0x7F); //하위 7비트 
			v >>>= 7; //오른쪽으로 7비트 shift
			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
				b |= 0x80;
				buffer.write(b);
			}else{
				buffer.write(b);
			}
			byteCnt++;
		} while (v != 0);
		
		return byteCnt;
	}
	
	public static int writeVariableByte(OutputStream os, int v) throws IOException {
		int byteCnt=0;
		do{
			int b = (byte)(v & 0x7F); //하위 7비트 
			v >>>= 7; //오른쪽으로 7비트 shift
			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
				b |= 0x80;
				os.write(b);
			}else{
				os.write(b);
			}
			byteCnt++;
		} while (v != 0);
		
		return byteCnt;
	}
	
//	public static int writeVariableByte(Output output, int v) throws IOException {
//		int byteCnt=0;
//		do{
//			int b = (byte)(v & 0x7F); //하위 7비트 
//			v >>>= 7; //오른쪽으로 7비트 shift
//			if(v != 0){ //데이터가 남았으면 최상위 비트에 1을 표시한다.
//				b |= 0x80;
//				output.writeByte(b);
//			}else{
//				output.writeByte(b);
//			}
//			byteCnt++;
//		} while (v != 0);
//		
//		return byteCnt;
//	}
	
	//입력정수의 가변길이를 계산한다.
	public static int lenVariableByte(int v) {
		int byteCnt=1;
		//7바이트씩 몇번 shift를 하게되는지 계산하면 된다. 부호비트를 유지하지 않는 logical shift를 이용한다.
		while((v >>>= 7)!=0)
			byteCnt++;
		
		return byteCnt;
	}
	
	public static int writeUChars(BytesBuffer buffer, char[] v) {
		for (int i = 0; i < v.length; i++) {
			writeUChar(buffer, v[i]);
		}
		
		return v.length * 2;
	}
	
	public static int writeAChars(BytesBuffer buffer, char[] v) {
		for (int i = 0; i < v.length; i++) {
			buffer.write(v[i] & 0xFF);
		}
		
		return v.length;
	}

	public static int writeAChars(BytesBuffer buffer, char[] v, int length) {
		int strLen = (v.length < length) ? v.length : length;
		
		for (int i = 0; i < strLen; i++) {
			buffer.write(v[i] & 0xFF);
		}
		//문자열 길이가 length보다 클경우 이 루프는 돌지 않는다.
		for (int i = strLen; i < length; i++) {
			buffer.write(0);
		}
		
		return length;
	}
	
	public static int writeUChars(BytesBuffer buffer, char[] v, int length) {
		int strLen = (v.length < length) ? v.length : length;
		
		for (int i = 0; i < strLen; i++) {
			writeUChar(buffer, v[i]);
		}
		//문자열 길이가 length보다 클경우 이 루프는 돌지 않는다.
		for (int i = strLen; i < length; i++) {
			writeUChar(buffer, 0);
		}
		
		return length * 2;
	}
	

	public static long readLong(BytesBuffer buffer) {
		return ((long) (readInt(buffer)) << 32) + (readInt(buffer) & 0xFFFFFFFFL);
	}

	public static long readLong(byte[] buffer, int pos) {
		return ((buffer[pos] & 0xff) << 56) + ((buffer[pos + 1] & 0xff) << 48) + ((buffer[pos + 2] & 0xff) << 40) + ((buffer[pos + 3] & 0xff) << 32)
				+ ((buffer[pos + 4] & 0xff) << 24) + ((buffer[pos + 5] & 0xff) << 16) + ((buffer[pos + 6] & 0xff) << 8) + ((buffer[pos + 7] & 0xff) << 0);
	}
	
	public static int readInt(BytesBuffer buffer) {
		return (buffer.read() << 24) + (buffer.read() << 16) + (buffer.read() << 8) + (buffer.read() << 0);
	}
	public static int readInt(ByteBuffer buffer) {
		return ((buffer.get() & 0xff) << 24)
		+ ((buffer.get() & 0xff) << 16)
		+ ((buffer.get() & 0xff) << 8)
		+ ((buffer.get() & 0xff) << 0);
	}
	public static int readInt(byte[] buffer, int pos) {
		return ((buffer[pos + 0] & 0xff) << 24) + ((buffer[pos + 1] & 0xff) << 16) + ((buffer[pos + 2] & 0xff) << 8) + ((buffer[pos + 3] & 0xff) << 0);
	}
	public static short readShort(BytesBuffer buffer) {
		return (short)((buffer.read() << 8) + (buffer.read() << 0));
	}
	public static short readShort(byte[] buffer, int pos) {
		return (short)(((buffer[pos] & 0xff) << 8) + ((buffer[pos + 1] & 0xff) << 0));
	}

	
	public static char readAChar(BytesBuffer buffer) {
		return (char)buffer.read();
	}

	public static char readUChar(BytesBuffer buffer) {
		return (char)readShort(buffer);
	}

	
	public static int readVariableByte(BytesBuffer buffer) {
		int v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = buffer.read();
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
	public static int readVariableByte(byte[] buffer, int pos) {
		int v = 0;
		int b = 0;
		int shift = 0;
		do{
			b = buffer[pos++];
			v |= ((b & 0x7F) << shift);
			shift += 7;
		} while((b & 0x80) > 0);
		
		return v;
	}
	
	public static char[] readAChars(BytesBuffer buffer) {
		int len = buffer.remaining();
		char[] cs = new char[len];
		
		for (int i = 0; i < len; i++)
			cs[i] = readAChar(buffer);
		return cs;
	}
	
	public static char[] readUChars(BytesBuffer buffer) {
		int len = buffer.remaining() / 2;
		char[] cs = new char[len];
		
		for (int i = 0; i < len; i++)
			cs[i] = readUChar(buffer);
		return cs;
	}
	
	public static char[] readAChars(byte[] data, int offset, int len) {
		char[] array = new char[len];
		for (int i = 0; i < len; i++)
			array[i] = (char) data[offset + i];
		
		return array;
	}
	
	public static char[] readUChars(byte[] data, int offset, int len) {
		char[] array = new char[len / 2];
		for (int i = 0; i < array.length; i++){
			array[i] = (char)(((data[offset + i * 2] & 0xff) << 8) + ((data[offset + i * 2 + 1] & 0xff) << 0));
		}
		
		return array;
	}
	
	
	public static int readAChars(BytesBuffer buffer, char[] data, int offset, int len) {
		for (int i = 0; i < len; i++)
			data[offset + i] = (char)buffer.read();
		
		return len;
	}
	
	public static int readUChars(BytesBuffer buffer, char[] data, int offset, int len) {
		for (int i = 0; i < len; i++)
			data[offset + i] = readUChar(buffer);
		
		return len;
	}

	/*
	 * output에 buffer의 데이터를 기록한다.
	 * pos 시작위치
	 * len 버퍼 데이터길이
	 * size 고정데이터 길이. len이 길이보다 작으면 0으로 채워넣는다.
	 * */
//	public static int writebytes(Output output, BytesBuffer buffer, int pos, int len, int size) throws IOException {
//		if(len >= size){
//			output.writeBytes(buffer.array(), pos, size);
//		}else{
//			output.writeBytes(buffer.array(), pos, len);
//			for (int i = 0; i < size - len; i++) {
//				output.writeByte(0);
//			}
//		}
//		
//		return size;
//	}	
	public static boolean merge(File segmentDir1, File segmentDir2, File targetDir, String filename) throws IOException{
		FileChannel fc1 = new FileInputStream(new File(segmentDir1, filename)).getChannel();
		FileChannel fc2 = new FileInputStream(new File(segmentDir2, filename)).getChannel();
		FileChannel targetFc = new FileOutputStream(new File(targetDir, filename)).getChannel();
		
		long count1 = fc1.size();
		fc1.transferTo(0, count1, targetFc);
		long appendSize = fc2.size();
		fc2.transferTo(0, appendSize, targetFc);
		
		fc1.close();
		fc2.close();
		targetFc.close();
		return true;
	}
	
	public static boolean copy(File a, File b) throws IOException{
		FileChannel fc1 = new FileInputStream(a).getChannel();
		if(b.exists())
			b.delete();
		FileChannel fc2 = new FileOutputStream(b).getChannel();
		
		long count1 = fc1.size();
		fc1.transferTo(0, count1, fc2);
		
		fc1.close();
		fc2.close();
		return true;
	}
	
	public static void quickSort(int[] numbers, int first, int last) {
		if(last <= 0)
			return;
		
		int stackMaxSize = (int) ((Math.log(last - first + 1) + 3) * 2);
		int[][] stack = new int[stackMaxSize][2];
		
		int pivot = 0, sp = 0;
		int left = 0, right = 0;
		
		while(true){
			while(first < last){
			    left = first;
			    right = last;
			    int median = (left + right)/2;
				
				//move pivot to left most. 
				int tmp = numbers[left];
				numbers[left] = numbers[median];
				numbers[median] = tmp;
				pivot = numbers[left];
				
			    while (left < right) {
			    	while ((numbers[right] - pivot) >= 0 && (left < right)) 
			            right --;
			    	
			        if (left != right){ 
			             numbers[left] = numbers[right];
			             left++;
			        } 
			        
			        while ((numbers[left] - pivot) <= 0 && (left < right)) 
			            left ++;
			        
			        if (left != right) {
			             numbers[right] = numbers[left];
			             right --;
			        } 
			    } 
			    
			    numbers[left] = pivot;
			    
			    if(left - first < last - left){
				    if (left + 1 < last) {
				    	sp++;
				    	stack[sp][0] = left + 1;
						stack[sp][1] = last;
				    }
				    last = left - 1;
			    }else{
			    	if (first < left - 1) {
				    	sp++;
				    	stack[sp][0] = first;
						stack[sp][1] = left -1;
				    }
				    first = left + 1;
			    }
			    
			}
			
			if (sp == 0) {
				return;
			}else {
				first = stack[sp][0];
				last = stack[sp][1];
				sp--;
			}
			
		}
		
	}
	
}
