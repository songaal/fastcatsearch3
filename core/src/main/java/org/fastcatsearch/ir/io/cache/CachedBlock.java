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

public class CachedBlock {
	public int block;
	public byte[] buf;
	public CachedBlock next; // hash bucket 리스트 다음 원소
	public CachedBlock after; // circular linked list 의 다음 원소	
	public CachedBlock before;
	
	public CachedBlock(){ }
	
	public CachedBlock(int block, byte[] blockData) {
		this.block = block;
		buf = blockData;
	}

	public void remove(){
		before.next = after;
		after.before = before;
	}
	
	public int size(){
		return buf.length + 16 + 30;//overhead 30, pointer 4개
	}
}
