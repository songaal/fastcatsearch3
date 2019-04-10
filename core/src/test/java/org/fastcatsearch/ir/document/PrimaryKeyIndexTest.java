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

package org.fastcatsearch.ir.document;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexWriter;



import junit.framework.TestCase;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimaryKeyIndexTest extends TestCase{
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexTest.class);

	String homePath = "/tmp/testHome/";
	File testDir= new File(homePath+"test0/");


	public void testSearchIntegerKey() throws IOException {
		int[] list = new int[]{1762510, 1762593, 1762615, 1764094};
		File testDir = new File("/Users/swsong/TEST_HOME/fastcatsearch-3.18.1/collections/ASSEMBLYGALLERY2/data/index0/a1");
		String filename = "primarykey.map";
		PrimaryKeyIndexReader reader = new PrimaryKeyIndexReader(testDir, filename);
		byte[] data = new byte[4];
		for (int pk : list ) {
			writeInt(data, pk);
			int docNo = reader.get(data);
			logger.info("doc[{}] pk[{}] buf[{}]", docNo, pk, data);
		}
	}

	@Test
	public void testListPK() throws IOException {
		String filePath = "/Users/swsong/TEST_HOME/fastcatsearch-3.18.1/collections/ASSEMBLYGALLERY2/data/index0/a1/primarykey.map";
		PrimaryKeyIndexBulkReader reader = new PrimaryKeyIndexBulkReader(new File(filePath));
		BytesBuffer buf = new BytesBuffer(4);
		int docNo = -1;
		while((docNo = reader.next(buf)) != -1) {
			buf.flip();
			int pk = IOUtil.readInt(buf);
			buf.clear();
//			int pk = 0;
			logger.info("doc[{}] pk[{}] buf[{}]", docNo, pk, buf);
		}
		reader.close();

	}
	private void writeInt(byte[] arr, int i) {
		arr[0] = (byte) (i >> 24);
		arr[1] = (byte) (i >> 16);
		arr[2] = (byte) (i >> 8);
		arr[3] = (byte) (i >> 0);
	}
	public void testWriteAndReadIntegerKey() throws IOException {
		///write
//		Random r = new Random(System.nanoTime());
		String filename = "pkmap";
		int indexInterval = 128;
		int len = 5;
		int START = 1000000;
		int TEST = 1000000;
		byte[][] expectedData = new byte[TEST][];
		long st = System.currentTimeMillis();


		PrimaryKeyIndexWriter writer = new PrimaryKeyIndexWriter(testDir, filename, indexInterval, 16 * 1024);
		for (int i = 0; i < TEST; i++) {
			byte[] data = new byte[len];
			int num = i + START;
			writeInt(data, num);
			expectedData[i] = data;
			int prev = writer.put(data, 0, data.length, i);
			if (prev >= 0) {
				for (int k = 0; k < len; k++)
					System.out.print(data[k] + ", ");
				System.out.println("deleted = " + prev + " ==> " + i);
			}
			if (i % 100000 == 0)
				System.out.println("write " + i + "...");
		}
		writer.write();
		writer.close();
		System.out.println("write time = " + (System.currentTimeMillis() - st));


		///read
		st = System.currentTimeMillis();
		PrimaryKeyIndexReader reader = new PrimaryKeyIndexReader(testDir, filename);
		System.out.println("read load = " + (System.currentTimeMillis() - st));
		st = System.currentTimeMillis();
		long stTotal = System.currentTimeMillis();

		for (int i = 0; i < TEST; i++) {
			int num = reader.get(expectedData[i]);
//			assertEquals(docNo, num);
			if (i % 100000 == 0) {
				System.out.print("search " + i + "...");
				System.out.println("t = " + (System.currentTimeMillis() - st) + "ms");
				st = System.currentTimeMillis();
			}
			if (i != num) {
				for (int k = 0; k < len; k++)
					System.err.print(expectedData[i][k] + ", ");
				System.err.println();
				System.err.println("ERROR >> " + i + " , " + num);
			}

		}
		System.out.println("read total = " + (System.currentTimeMillis() - stTotal) + "ms");
	}


	public void testWriteAndRead() throws IOException{
		///write
		Random r = new Random(System.currentTimeMillis());
		String filename = "pkmap";
		int indexInterval = 128;
		int len = 5;
		int TEST = 1000000;
		byte[][] expectedData= new byte[TEST][];
		long st = System.currentTimeMillis();
		
		
		PrimaryKeyIndexWriter writer = new PrimaryKeyIndexWriter(testDir, filename, indexInterval, 16 * 1024);
		for(int docNo=0;docNo < TEST;docNo++){
			byte[] data = new byte[len];
			r.nextBytes(data);
			expectedData[docNo] = data;
			int prev= writer.put(data, 0, data.length, docNo);
			if(prev >= 0){
				for(int k=0;k<len;k++)
					System.out.print(data[k]+", ");
				System.out.println("deleted = "+prev+" ==> "+docNo);
			}
			if(docNo % 100000 == 0)
				System.out.println("write "+docNo+"...");
		}
		writer.write();
		writer.close();
		System.out.println("write time = "+(System.currentTimeMillis() -st));
		
		
		///read
		st = System.currentTimeMillis();
		PrimaryKeyIndexReader reader = new PrimaryKeyIndexReader(testDir, filename);
		System.out.println("read load = "+(System.currentTimeMillis() -st));
		st = System.currentTimeMillis();
		long stTotal = System.currentTimeMillis();
		
		for(int docNo=0;docNo < TEST;docNo++){
			int num = reader.get(expectedData[docNo]);
//			assertEquals(docNo, num);
			if(docNo % 100000 == 0){
				System.out.print("search "+docNo+"...");
				System.out.println("t = "+(System.currentTimeMillis() -st)+"ms");
				st = System.currentTimeMillis();
			}
			if(docNo != num){
				for(int k=0;k<len;k++)
					System.out.print(expectedData[docNo][k]+", ");
				System.out.println();
				System.out.println(">> "+docNo+" , "+num);
			}
			
		}
		System.out.println("read total = "+(System.currentTimeMillis() - stTotal)+"ms");
	}
	
	public void testDuplicated2() throws IOException{
		///write
		String filename = "pkmap-dup2";
		int indexInterval = 128;
		byte[] dd= new byte[]{1,3,5,7,9};
		int TEST = 5;
		
		
		PrimaryKeyIndexWriter writer = new PrimaryKeyIndexWriter(testDir, filename, indexInterval, 16 * 1024);
		for(int docNo=0;docNo < TEST;docNo++){
			writer.put(dd, 0, dd.length, docNo);
		}
		writer.write();
		writer.close();
		
		
		///read
		PrimaryKeyIndexReader reader = new PrimaryKeyIndexReader(testDir, filename);
		
		for(int docNo=0;docNo < TEST;docNo++){
			int num = reader.get(dd);
//			assertEquals(docNo, num);
			if(docNo != num){
				System.out.println(docNo+" , "+num);
			}
			
		}
	}
	public void testDuplicated() throws IOException{
		///write
		String filename = "pkmap-dup";
		int indexInterval = 128;
		byte[]expectedData= new byte[]{1,3,5,7,9};
		
		PrimaryKeyIndexWriter writer = new PrimaryKeyIndexWriter(testDir, filename, indexInterval, 16 * 1024);
		int prevNo = writer.put(expectedData, 0, expectedData.length, 0);
		System.out.println("prevNo="+prevNo);	
		prevNo = writer.put(expectedData, 0, expectedData.length, 3);
		System.out.println("prevNo="+prevNo);	
		prevNo = writer.put(expectedData, 0, expectedData.length, 5);
		System.out.println("prevNo="+prevNo);
		prevNo = writer.put(expectedData, 0, expectedData.length, 27);
		System.out.println("prevNo="+prevNo);
		prevNo = writer.put(expectedData, 0, expectedData.length, 31);
		System.out.println("prevNo="+prevNo);
		writer.write();
		writer.close();
		
		///read
		PrimaryKeyIndexReader reader = new PrimaryKeyIndexReader(testDir, filename);
		int num = reader.get(expectedData);
		System.out.println("num="+num);
		num = reader.get(expectedData);
		System.out.println("num="+num);	
	}
	
	public void testLarge() throws IOException{
		///write
		Random r = new Random(System.currentTimeMillis());
		int len = 4;
		int len2 = 10;
		int TEST = 500000;
		byte[][] expectedData= new byte[TEST][];
		long st = System.currentTimeMillis();
		
		
		PrimaryKeyIndexWriter writer = new PrimaryKeyIndexWriter(16 * 1024);
		PrimaryKeyIndexWriter writer2 = new PrimaryKeyIndexWriter(16 * 1024);
		for (int docNo = 0; docNo < TEST; docNo++) {
			byte[] data = new byte[len];
			byte[] data2 = new byte[len2];
			r.nextBytes(data);
			r.nextBytes(data2);
//			expectedData[docNo] = data;
			int prev = writer.put(data, 0, data.length, docNo);
			writer.put(data2, 0, data2.length, docNo);
//			System.out.println("######"+docNo + " , prev = "+prev);
//			if(prev >= 0){
//				for(int k=0;k<len;k++)
//					System.out.print(data[k]+", ");
//				System.out.println("deleted = "+prev+" ==> "+docNo);
//			}
			
			if(docNo % 100000 == 0)
				System.out.println("write "+docNo+"...");
		}
		System.out.println("write time = "+(System.currentTimeMillis() -st));
	}
	
	public void testEmptyString() throws IOException{
		///write
		File testDir = new File("/Users/swsong/tmp");
		String filename = "group.PRODUCTBRAND.pk";
		byte[] key2 = new byte[0];
		
		
		///read
		PrimaryKeyIndexReader reader = new PrimaryKeyIndexReader(testDir, filename);
		
		int num = reader.get(key2);
		System.out.println("key2 > " + num);
		reader.close();
	}
}
