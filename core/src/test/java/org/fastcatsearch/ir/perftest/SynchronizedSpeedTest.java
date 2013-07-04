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

package org.fastcatsearch.ir.perftest;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;

public class SynchronizedSpeedTest extends TestCase {
	public void test1(){
		int SIZE = 5000000;
		
		long st = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			synchronized(this){
				Integer.parseInt(i+"");
			}
		}
		System.out.println("case1 "+(System.currentTimeMillis() - st));
		
		Lock lock = new ReentrantLock();
		st = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
//			try{
				lock.lock();
				Integer.parseInt(i+"");
//			}finally{
				lock.unlock();
//			}
		}
		
		System.out.println("case2 "+(System.currentTimeMillis() - st));
	}
}
