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

import org.fastcatsearch.ir.search.HitElement;

/**
 * 고정 사이즈 선입선출 구조 
 * @author sangwook.song
 *
 */
public class FixedHitQueue {
	private HitElement[] list;
	private int head;
	private int tail;
	
	public FixedHitQueue(int size){
		list = new HitElement[size];
	}
	
	public HitElement[] getHitElementList(){
		return list;
	}
	
	public boolean push(HitElement e){
		list[tail++] = e;
		return true;
	}
	
	public HitElement pop(){
		if(head == tail)
			return null;
		
		return list[head++];
	}
	
	public int size(){
		return tail - head;
	}
	
	public boolean skip(int n){
		if(n + head < tail){
			head += n;
			return true;
		}
		
		return false;
	}
	
	public FixedHitReader getReader(){
		return new FixedHitReader(list, head, tail);
	}
}
