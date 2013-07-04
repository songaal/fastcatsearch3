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

import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BitSetBulkReader;

import junit.framework.TestCase;


public class BitSetBulkReaderTest extends TestCase{
	public void test1(){
		int MAX = 4000000;
		int[] testcase = new int[MAX];
		
		BitSet set = new BitSet();
		Random r = new Random(System.currentTimeMillis());
		
		System.out.println("==set==");
		testcase[0] = 1;
		set.set(testcase[0]);
		for (int i = 1; i < MAX; i++) {
			testcase[i] = testcase[i - 1] + ((i%2==0)?2:5);
			set.set(testcase[i]);
		}

		System.out.println("==read==");
		BitSetBulkReader bulkReader = new BitSetBulkReader(set);
		
		for (int i = 0; i < MAX; i++) {
			int id = bulkReader.next();
//			System.out.println(id+ " = "+testcase[i]);
			assertEquals(id, testcase[i]);
		}
	}
	
}
