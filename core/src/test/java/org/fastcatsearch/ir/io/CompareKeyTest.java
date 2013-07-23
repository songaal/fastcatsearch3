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
import java.io.IOException;
import java.nio.ByteBuffer;

import org.fastcatsearch.ir.document.PrimaryKeyIndexWriter;
import org.fastcatsearch.ir.document.merge.PrimaryKeyIndexMerger;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;

import junit.framework.TestCase;


public class CompareKeyTest extends TestCase {

	//PrimaryKeyIndexWriter 에서 pk를 정렬시 잘 안되는 현상발견.
	//compare가 의심이 가서 테스트를 진행함.
	//2012-5-4 swsong
	public void testForPrimaryKeyIndexWriter(){
		BytesBuffer buffer1 = new BytesBuffer(4);
		BytesBuffer buffer2 = new BytesBuffer(4);
		int AA = 127;
		for (int i = 0; i < 1000; i++) {
			AA = i;
			buffer1.clear();
			buffer2.clear();
			IOUtil.writeInt(buffer1, AA);
			IOUtil.writeInt(buffer2, i+1);
			buffer1.flip();
			buffer2.flip();
			int cmp = compareKey(buffer1.bytes, buffer2.bytes);
//			System.out.println("CMP = "+cmp);
			String t = " = ";
			if(cmp > 0){
				t = " > ";
			}else if(cmp < 0){
				t = " < ";
				System.out.println(AA+t+(i+1));
//				System.out.println(i+"=("+buffer1+")");
//				System.out.println((i+1)+"=("+buffer2+")");
			}
			
//			assertTrue(cmp < 0);
		}
		
		
	}
	
	public void testByteBuffer(){
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.put((byte) 0);
		buf.put((byte) -127);
		buf.flip();
		ByteBuffer buf2 = ByteBuffer.allocate(2);
		buf2.put((byte) 0);
		buf2.put((byte) 5);
		buf2.flip();
		
		int cmp = buf.compareTo(buf2);
		System.out.println("cmp = "+cmp);
	}
	//148608
	//148599
	/*
	 * 							148608 -- PK2
[2012-05-04 13:40:00,055] 463 / 148599 -- PK2
[2012-05-04 13:40:00,055] 462 / 148602 -- PK2
[2012-05-04 13:40:00,055] 461 / 148603 -- PK2
[2012-05-04 13:40:00,055] 460 / 148607 -- PK2
	 * */
	public void testTwoInt(){
		BytesBuffer buffer1 = new BytesBuffer(4);
		BytesBuffer buffer2 = new BytesBuffer(4);
		int AA = 148608;
//		int BB = 148608;
		int BB = 148609;
		buffer1.clear();
		buffer2.clear();
		IOUtil.writeInt(buffer1, AA);
		IOUtil.writeInt(buffer2, BB);
		buffer1.flip();
		buffer2.flip();
		int cmp = compareKey(buffer1.bytes, buffer2.bytes);
		String t = " = ";
		if(cmp > 0){
			t = " > ";
		}else if(cmp < 0){
			t = " < ";
		}
		System.out.println(AA+t+BB);
		System.out.println("("+buffer1+")");
		System.out.println("("+buffer2+")");
			
	}
	/*
	 *  [2012-05-04 13:40:00,055] 446 / 148588 -- PK1
		[2012-05-04 13:40:00,055] 458 / 148599 -- PK1
		[2012-05-04 13:40:00,055] 457 / 148602 -- PK1
		[2012-05-04 13:40:00,055] 456 / 148603 -- PK1
		[2012-05-04 13:40:00,055] 455 / 148607 -- PK1
		
		[2012-05-04 13:40:00,055] 459 / 148608 -- PK2
		[2012-05-04 13:40:00,055] 463 / 148599 -- PK2
		[2012-05-04 13:40:00,055] 462 / 148602 -- PK2
		[2012-05-04 13:40:00,055] 461 / 148603 -- PK2
		[2012-05-04 13:40:00,055] 460 / 148607 -- PK2
	 * */
	public void testSimulation() throws IOException{
		int[] pk1List = new int[]{148588, 148599, 148602, 148603, 148607};
		int[] pk2List = new int[]{148599, 148602, 148603, 148607, 148608};
		
		BytesBuffer buffer = new BytesBuffer(4);
		String f1 = "test.pk1";
		String f2 = "test.pk2";
		String f3 = "test.pk3";
		
		File dir = new File(".");
		
		
		//1. 테스트 pk파일 만들기.
		PrimaryKeyIndexWriter w = new PrimaryKeyIndexWriter(dir, f1, 128, 1024);
		for (int i = 0; i < pk1List.length; i++) {
			buffer.clear();
			IOUtil.writeInt(buffer, pk1List[i]);
			buffer.flip();
			w.put(buffer.bytes, 0, buffer.bytes.length, i);
		}
		w.write();
		w.close();
		
		w = new PrimaryKeyIndexWriter(dir, f2, 128, 1024);
		for (int i = 0; i < pk2List.length; i++) {
			buffer.clear();
			IOUtil.writeInt(buffer, pk2List[i]);
			buffer.flip();
			w.put(buffer.bytes, 0, buffer.bytes.length, i+100);
		}
		w.write();
		w.close();
		
		//2. 머징하기.
		
		PrimaryKeyIndexMerger m = new PrimaryKeyIndexMerger();
		m.merge(new File(dir, f1), new File(dir, f2), new File(dir, f3), 128, null);
	}
	private int compareKey(byte[] t, byte[] t2){
    	int length = t.length;
    	
		for (int i = 0; i < length; i++) {
			if(t[i] != t2[i]){
				System.out.println((t[i])+" : "+t2[i]);
				System.out.println((t[i] & 0xFF)+" : "+(t2[i] & 0xFF));
				System.out.println((int)(t[i])+" : "+(int)t2[i]);
				return (t[i] & 0xFF) - (t2[i] & 0xFF);
			}
		}
		
		return 0;
    }
	
	
    
}
