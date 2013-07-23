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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;

import junit.framework.TestCase;


public class IOUtilTest extends TestCase{
	Random r = new Random();
	int TESTNUM = 10;
	
	public void _testInt() throws IOException{
		BytesBuffer buffer = new BytesBuffer(4);
		
		for(int i=0;i<TESTNUM;i++){
			int expected = r.nextInt();
			IOUtil.writeInt(buffer, expected);
			buffer.clear();
			int actual = IOUtil.readInt(buffer);
			buffer.clear();
			assertEquals(expected, actual);
		}
	}
	
	public void testVariableByteCode() throws IOException{
		BytesBuffer buffer = new BytesBuffer(10);
		
		for(int i=0;i<TESTNUM;i++){
			int expected = r.nextInt();
			int len = 0;
			IOUtil.writeVInt(buffer, expected);
			buffer.clear();
			int actual = IOUtil.readVInt(buffer);
			buffer.clear();
			assertEquals(expected, actual);
			assertEquals(len, IOUtil.lenVariableByte(expected));
			
		}
	}
	
	
	
	public void testVBLen(){
		BytesBuffer buf = new BytesBuffer(5);
		
		for (int i = 100000000; i < Integer.MAX_VALUE; i++) {
			buf.clear();
			IOUtil.writeVInt(buf, i);
			buf.flip();
			
			int len = IOUtil.lenVariableByte(i);
			
			assertEquals(buf.limit(), len);
			
			if(i % 100000000 == 0){
				System.out.println("process.." + i+", /len="+len);
			}
		}		
	}
	
	public void testLong(){
		long expected = 401435124;
		BytesBuffer buf = new BytesBuffer(8);
		IOUtil.writeLong(buf, expected);
		
		
		buf.flip();
		long actual = IOUtil.readLong(buf.array(), 0);
		
		assertEquals(expected, actual);
		
	}
	
	public void testInt(){
		int expected = 11312123;
		BytesBuffer buf = new BytesBuffer(4);
		IOUtil.writeInt(buf, expected);
		buf.flip();
		
//		int t = 0;//(buf.read() << 24) + (buf.read() << 16) + (buf.read() << 8) + (buf.read() << 0);
//		for (int i = 0; i < 4; i++) {
//			int b = buf.read();
//			int c = buf.array[i] & 0xff;
//			t += b << (8 * (3 - i));
//			System.out.println(i+" = "+b+", "+c);
////			<< "+t);
//		}
//		byte[] arr = buf.array;
//		int pos = 0;
//		int t2 = (arr[pos + 0] << 24) + (arr[pos + 1] << 16) + (arr[pos + 2] << 8) + (arr[pos + 3] << 0);
		
//		System.out.println(">>"+t+", "+t2);
		
		int actual = IOUtil.readInt(buf);
		assertEquals(expected, actual);
		
//		
		actual = IOUtil.readInt(buf.array(), 0);
		assertEquals(expected, actual);
		
	}
}
