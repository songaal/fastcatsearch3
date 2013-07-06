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

import java.util.Random;

import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;

import junit.framework.TestCase;

public class CompareBufferTest extends TestCase {
	public void testSameSignNumber(){
		int a = 1235;
		int b = 234567;
		
		BytesBuffer ab = new BytesBuffer(4);
		IOUtil.writeInt(ab, a);
		ab.flip();
		
		BytesBuffer bb = new BytesBuffer(4);
		IOUtil.writeInt(bb, b);
		bb.flip();
		
		System.out.println(ab);
		makeCompliment(ab);
		System.out.println(ab);
		makeCompliment(ab);
		System.out.println(ab);
		System.out.println("-----------------------");
		System.out.println(bb);
		makeCompliment(bb);
		System.out.println(bb);
	}
	
	public void testCompliment(){
		BytesBuffer ab = new BytesBuffer(4);
		for (int i = -10; i < 10; i++) {
			ab.clear();
			IOUtil.writeInt(ab, i);
			ab.flip();
			System.out.println(i+" -----------------------");
			System.out.println(ab);
			makeCompliment(ab);
			System.out.println(ab);
			
		}
	}
	
	public void testCompare(){
		BytesBuffer ab = new BytesBuffer(4);
		BytesBuffer bb = new BytesBuffer(4);
		
		
//		int a = 12345;
//		int b = -12345;
		Random r = new Random();
		int [] testSet = {Integer.MIN_VALUE, -100000000, -10000000, -1000000, -100000, -10000, -1000, -100, -10, -1, 0, 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, Integer.MAX_VALUE};
		for (int k = 0; k < 1000; k++) {
			
			for (int i = 0; i < testSet.length; i++) {
				int a = testSet[i];
				int b = r.nextInt();
				
	//			if(i % 10000000 == 0)
				System.out.println(i+".. "+a + " : "+b);
				
				ab.clear();
				bb.clear();
				
				IOUtil.writeInt(ab, a);
				IOUtil.writeInt(bb, b);
				ab.flip();
				bb.flip();
				
				int R = a > b ? 1 : a == b ? 0 : -1;
				try{
					assertTrue(compareBuffer(ab.bytes, bb.bytes) == R);
				}catch(Error err){
					err.printStackTrace();
					System.out.println("a="+a+", b="+b);
					System.out.println(ab);
					System.out.println("----------------");
					System.out.println(bb);
				}
				makeCompliment(ab);
				makeCompliment(bb);
				assertTrue(compareBuffer(ab.bytes, bb.bytes) == -R);
			}	
		}
	}
	
	public void test1(){
		byte a= -126;
		byte b = -12;
		System.out.println(">>a = "+a);
		System.out.println(">>b = "+b);
		System.out.println(Integer.toBinaryString(a));
		System.out.println(Integer.toBinaryString(b));
		System.out.println(">>a & 0xff = "+ (a & 0xff));
		System.out.println(">>ㅠ & 0xff = "+ (b & 0xff));
		System.out.println(Integer.toBinaryString(a & 0xff));
		System.out.println(">>~(a & 0xff) = "+ ~(a & 0xff));
		System.out.println(Integer.toBinaryString(~(a & 0xff)));
		System.out.println(">>(byte) ~(a & 0xff) = "+ (byte) ~(a & 0xff));
		System.out.println(Integer.toBinaryString((byte)~(a & 0xff)));
		
//		System.out.println("(byte) (a & 0xff) = "+ (byte) (a & 0xff));
//		System.out.println(Integer.toBinaryString((a & 0xff)));
	}
	private void makeCompliment(BytesBuffer buffer){
		for (int i = buffer.pos(); i < buffer.limit(); i++) {
			buffer.bytes[i] = (byte) ~(buffer.bytes[i] & 0xff);
		}
	}
	
	private int compareBuffer(byte[] arr1, byte[] arr2){
		for (int i = 0; i < arr1.length; i++) {
			if(arr1[i] > arr2[i]){
				return 1;
			}else if(arr1[i] < arr2[i]){
				return -1;
			}
		}
		return 0;
	}
}
