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
 * Max Heap을 유지하면서 최소원소 top K개를 보관한다.
 * 원소입력이 다 끝나면 최소원소를 내림차순으로 pop할수 있다.
 * 이 원소들을 차례대로 리스트에 거꾸로 입력하면 오름차순으로 정렬된 리스트를 얻을수 있다.
 * 
 * 정렬이 안된 원소들중에서 상위 k개를 뽑아내는데 사용한다.즉, 모든 원소의 입력이 필요하다.
 * 하지만 만약 정렬 된 리스트들을 머징하려는 용도라면 이 클래스가 아닌 FixedMinHeap을 사용한다.
 * @author sangwook.song
 *
 */
public abstract class FixedMaxPriorityQueue<T> {
	protected static Logger logger = LoggerFactory.getLogger(FixedMaxPriorityQueue.class);
	
	private Object[] heap;
	private int maxsize;
	private int size;
	
	public FixedMaxPriorityQueue(int maxsize){
		this.maxsize = maxsize;
		heap = new Object[maxsize + 1];
	}
	//원소갯수
	public int size(){
		return size;
	}
	
	//
	// one과 two는 heapify하면서 한번이상 사용될 것이므로 내부속성이 바뀌어서는 안된다.
	//
	protected abstract int compare(T one, T two);
		
	public boolean push(T e){
		
		if (size < maxsize) {
			size++;
			heap[size] = e;
			upHeap();
		} else if (size > 0 && compare(peek(), e) > 0) {
			heap[1] = e;
			downHeap();
		} else{
			//reject
//			logger.debug("REJECT = "+e);
		}
		
		return true;
	}
	
	public T pop(){
		if (size > 0) {
			Object top = heap[1];
			heap[1] = heap[size];
			heap[size] = null;
			size--;
			downHeap();
			return (T) top;
		}
		
		return null;
	}
	
	public T peek(){
		if (size > 0)
			return (T) heap[1];
		
		return null;
	}
	
	public Object[] getSortedList(){
		Object[] sorted = new Object[size];
		//리스트에 거꾸로 입력한다. 
		for(int i=size-1;i>=0;i--){
			sorted[i] = pop();
		}
		return sorted;
	}
	
	public void print(){
		for (int i = 0; i < size; i++) {
			logger.info("{}", heap[i + 1]);
		}
	}
	
	private void upHeap() {
		int idx = size;
		Object node = heap[idx];
		int parent = idx >> 1;
		while ((parent > 0) && compare((T)node, (T)heap[parent]) > 0) {
			heap[idx] = heap[parent];
			idx = parent;
			parent = parent >> 1;
		}
		heap[idx] = node;
	}
	
	private void downHeap(){
		
		int child = -1;
		int idx = 1;
		
		while(idx <= size){
			int left = idx * 2;
			int right = left + 1;
			
			if(left <= size){
				if(right <= size){
					if(compare((T)heap[left], (T)heap[right]) > 0)
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
			if(compare((T)heap[child], (T)heap[idx]) > 0){
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
