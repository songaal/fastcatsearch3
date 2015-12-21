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

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.FixedMaxPriorityQueue;
import org.fastcatsearch.ir.search.DefaultRanker;
import org.fastcatsearch.ir.search.HitElement;


import junit.framework.TestCase;

public class FixedMaxPriorityQueueTest extends TestCase{
	public void testLook(){
		
		int ELEMENT_MAX = 10;
		int QUEUE_SIZE = 10;
		
		DefaultRanker queue = new DefaultRanker(QUEUE_SIZE);
//		int[] value = new int[]{853,969,634,900,370,582,593,854,290,606};
		
		HitElement[] els = new HitElement[ELEMENT_MAX];
		Random r = new Random();
		for(int i=0;i<ELEMENT_MAX;i++){
			byte[] rankdata = new byte[4]; 
			r.nextBytes(rankdata);
			els[i] = new HitElement(i, r.nextInt(1000), 3, new BytesRef[]{new BytesRef(rankdata)}, null);
			queue.push(els[i]);
			System.out.println(">>"+els[i]);
		}
		
		System.out.println("=== result ===");
		Object[] list = queue.getSortedList();
		for(int i=0;i<list.length;i++){
			System.out.println(i+"] "+list[i]);
		}
	}

	

}
