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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BitSet;

import junit.framework.TestCase;
import org.junit.Test;


public class BitSetTest extends TestCase {
	public void test1(){
		int MAX = 1000 * 10000;
		int[] testcase = new int[MAX];
		
		BitSet set = new BitSet();
		Random r = new Random(System.currentTimeMillis());
		
		for(int i=0;i<MAX;i++){
			testcase[i] = r.nextInt(10000000);
			set.set(testcase[i]);
		}
		
		for(int i=0;i<MAX;i++){
			int number = testcase[i];
			assertTrue(set.isSet(number));
		}
	}
	public void test2() throws IOException{
		File segmentDir = new File("/Users/swsong/search/fastcat_basic/collection/news/data/1");
		int revision = 45;
		System.out.println(segmentDir);
		BitSet deleteSet = new BitSet(segmentDir, IndexFileNames.docDeleteSet);
		for(int i=0; i<100;i++){
			System.out.println(i);
			if(deleteSet.isSet(i)){
				System.out.println(">> "+i);
			}
		}
	}
	public void testSaveLoad() throws InterruptedException, IOException{
		String filename = "doc.del";
		File dir = new File("testHome");
		
		int MAX = 1000 * 10000;
		int[] testcase = new int[MAX];
		
		BitSet set = new BitSet(dir, filename);
		Random r = new Random(System.currentTimeMillis());
		
		for(int i=0;i<MAX;i++){
			testcase[i] = r.nextInt(10000000);
			set.set(testcase[i]);
		}
		
		for(int i=0;i<MAX;i++){
			int number = testcase[i];
			assertTrue(set.isSet(number));
		}
		
		set.save();
		
		Thread.sleep(1000);
		
		BitSet set2 = new BitSet(dir, filename);
		for(int i=0;i<MAX;i++){
			int number = testcase[i];
			assertTrue(set2.isSet(number));
		}
		
		new File(dir, filename).delete();
	}

    @Test
    public void testGetOnCount() throws IOException {
        BitSet bitset = new BitSet(new File("/Users/swsong/TEST_HOME/fastcatsearch-segment/collections/film/data/index0/vy/delete.set"),false);
        int onCount = bitset.getOnCount();
        onCount = bitset.getOnCount();
        onCount = bitset.getOnCount();
        onCount = bitset.getOnCount();
        onCount = bitset.getOnCount();
        onCount = bitset.getOnCount();
        System.out.println("onCount : " + onCount);

    }

    @Test
    public void test10() {
        BitSet set = new BitSet();
        int S = 3000;
        int E = 3100;
        for (int i = S; i < E; i++) {
            if(!set.isSet(i)) {
                set.set(i);
            }
        }

        for (int i = 0; i < E; i++) {
            if(set.isSet(i)) {
                System.out.println(i);
            }
        }
    }
	public static void main(String[] args) throws IOException {
		String line = null;
		System.out.println(">>");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while((line = reader.readLine()) != null){
			if(line.length() == 0)
				break;
			
			BitSet set = new BitSet(new File(line));
			
			for (int i = 0; i < 160000; i++) {
				if(set.isSet(i))
					System.out.println(i);
				
			}
			
			System.out.println(">>");
		}
		
		System.out.println("End..");
		
		
	}
	
	
}
