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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sangwook.song
 *
 */
public class FixedMinHeap<T extends Comparable<T>> {
	protected static Logger logger = LoggerFactory.getLogger(FixedMinHeap.class);
	
	protected Object[] heap;
	protected int maxsize;
	protected int size;
	
	public FixedMinHeap(int maxsize){
		this.maxsize = maxsize;
		heap = new Object[maxsize + 1];
	}
	//원소갯수
	public int size(){
		return size;
	}
	
	public boolean push(T e){
		
		if (size < maxsize) {
			size++;
			heap[size] = e;
			upHeap();
//			logger.debug("PUSHBACK = "+e);
			return true;
		}
		return false;
//		logger.debug("TOP = "+heap[1]);
	}
	
	public T pop(){
		if (size > 0) {
			Object top = heap[1];
			heap[1] = heap[size];
			heap[size] = null;
			size--;
			heapify();
			return (T) top;
		}
		
		return null;
	}
	
	public T peek(){
		if (size > 0)
			return (T) heap[1];
		
		return null;
	}
	
	public void print(){
		for (int i = 0; i < size; i++) {
			logger.debug("{}", heap[i + 1]);
		}
	}
	
	private void upHeap() {
		int idx = size;
		Object node = heap[idx];
		int parent = idx >> 1;
//		while ((parent > 0) && ((T)node).compareTo((T)heap[parent]) < 0) {
		while ((parent > 0) && compareTo((T)node ,(T)heap[parent]) < 0) {
			heap[idx] = heap[parent];
			idx = parent;
			parent = parent >> 1;
		}
		heap[idx] = node;
	}
	
	//
	// 정책에 따라 compareTo 를 상속하여 변경할수 있다.
	//
	protected int compareTo(T a, T b){
		return a.compareTo(b);
	}
	
	public void heapify(){
		
		int child = -1;
		int idx = 1;
		
		while(idx <= size){
			int left = idx * 2;
			int right = left + 1;
			
			if(left <= size){
				if(right <= size){
					if(compareTo((T)heap[left], (T)heap[right]) < 0)
						child = left;
					else
						child = right;
				}else{
					//if there is no right el.
					child = left;
				}
			}else{
				//no children
				break;
			}
			
			//현재 노드와 child와 비교해서 swap한다.
			if(compareTo((T)heap[child], (T)heap[idx]) < 0){
				Object temp = heap[child];
				heap[child] = heap[idx];
				heap[idx] = temp;
				idx = child;
			}else{
				//같거나 정렬되어 있으면 child를 확인하지 않는다.
				break;
			}
					
		}
	}
	
}
