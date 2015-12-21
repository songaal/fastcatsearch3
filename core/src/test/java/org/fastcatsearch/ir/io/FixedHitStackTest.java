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

import org.fastcatsearch.ir.io.FixedHitQueue;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedHitStack;
import org.fastcatsearch.ir.search.HitElement;

import junit.framework.TestCase;


public class FixedHitStackTest extends TestCase{
	public void testStack(){
		int SIZE = 10;
		FixedHitStack stack = new FixedHitStack(SIZE);
		for(int i=0;i<SIZE;i++){
			stack.push(new HitElement(i,i,3,null));
		}
		
		for(int i=0;i<SIZE;i++){
			HitElement e = stack.pop();
			assertEquals(SIZE - i - 1, e.docNo());
			assertEquals(SIZE - i - 1, e.score());
		}
	}
	
	public void testStack2(){
		int SIZE = 10;
		FixedHitStack stack = new FixedHitStack(SIZE);
		int REAL_SIZE = 5;
		for(int i=0;i<REAL_SIZE;i++){
			stack.push(new HitElement(i,i,3,null));
		}
		
		for(int i=0;i<REAL_SIZE;i++){
			HitElement e = stack.pop();
			assertEquals(REAL_SIZE - i - 1, e.docNo());
			assertEquals(REAL_SIZE - i - 1, e.score());
		}
	}
	
	public void testStackHitReader(){
		int SIZE = 10;
		FixedHitStack stack = new FixedHitStack(SIZE);
		for (int i = 0; i < SIZE; i++) {
			int v = SIZE - i - 1;
			stack.push(new HitElement(v,v,3,null));
		}
		
		FixedHitReader reader = stack.getReader();
		int k = 0;
		while(reader.next()){
			HitElement e = reader.read();
			assertEquals(k, e.docNo());
			assertEquals(k, e.score());
			k++;
		}
	
	}
	
	public void testStackHitReader2(){
		int SIZE = 10;
		FixedHitStack stack = new FixedHitStack(SIZE);
		int REAL_SIZE = 5;
		for (int i = 0; i < REAL_SIZE; i++) {
			int v = REAL_SIZE - i - 1;
			stack.push(new HitElement(v,v,3,null));
		}
		
		FixedHitReader reader = stack.getReader();
		int k = 0;
		while(reader.next()){
			HitElement e = reader.read();
			assertEquals(k, e.docNo());
			assertEquals(k, e.score());
			k++;
		}
	
	}
	
	
}
