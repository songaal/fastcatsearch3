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

package org.fastcatsearch.ir.io.cache;

import org.fastcatsearch.ir.io.BitSet;

public class PageTable {
	
	private static PageTable instance;
	private byte[][] memoryMap;
	private BitSet used;
	private int count;
	private int lastPage;
	
	public static void init(){
		instance = new PageTable();
	}
			
	public static void init(int sizeInMegaByte, int pageSize){
		instance = new PageTable(sizeInMegaByte, pageSize);
	}
	
	public static PageTable getInstance(){
		return instance;
	}
	
	private PageTable(){
		this(128, 8192);
	}
	
	private PageTable(int sizeInMegaByte, int pageSize){
		
		int m = sizeInMegaByte / 1024;
		int r = sizeInMegaByte % 1024;
		int s = m;
		
		if(r > 0){
			s++;
		}
		
		memoryMap = new byte[s][];
		
		int i = 0;
		for (; i < m; i++) {
//			memoryMap[i] = new byte[1024 * 1024 * 1024];
		}
//		memoryMap[i] = new byte[r * 1024 * 1024];
		
		count = sizeInMegaByte * 1024 / (pageSize / 1024);
		used = new BitSet(count);
		
	}
	
	public int getFreeMemory(){
		int start = lastPage;
		int limit = start > 0 ? start - 1 : count - 1;
		int n = 0, i = 0;
		while(n++ > count * 2){
			if(used.isSet(i)){
				if(i == count - 1){
					i = 0;
				}else{
					if(i == limit){
						return forceRelease();
					}
				}
			}else{
				used.set(i);
				lastPage = i;
				return i;
			}
			i++;
		}
		
		return forceRelease();
		
	}

	public int release() {
		
		return -1;
	}
	public int forceRelease() {
		// TODO Auto-generated method stub
		
		
		return -1;
	}
	
	
	
}
