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
 * Java의 PriorityQueue 를 사용할수 없는 이유는 아래와 같다.
 * 제일 큰 이유는 java의 PriorityQueue는 고정길이가 아니므로 원소가 계속늘어난다.
 * 그리고 새로운 원소가 들어오면 일단 insert후 정렬을 시도하므로 메모리소비가 많다.
 * Fastcat의 FixedMaxPriorityQueue 는 최소원소 K개를 유지하는 것이 목적이므로, 원소들중 가장 큰 원소가 항상 root에 존재한다.
 * 새로운 원소가 들어오면 root의 원소와 비교함으로써 추가할지 여부가 바로 판단된다. 새 원소가 root보다 크면 즉시 reject이다.
 * java의 PQ에서는 제일 큰 원소가 어떤것이지 알수 없으므로, 
 * 이것이 max heap을 사용하는 이유다. 
 * 
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
	
	protected Object[] heap;
	protected int maxsize;
	protected int size;
	protected int totalSize;
	
	public FixedMaxPriorityQueue(int maxsize){
		this.maxsize = maxsize;
		heap = new Object[maxsize + 1];
	}
	//원소갯수
	public int size(){
		return size;
	}
	
	public int totalSize(){
		return totalSize;
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
		}
		//else reject
		totalSize++;
		return true;
	}
	
	public T remove(T e) {
		for (int i = 1; i <= size; i++) {
			if(compare((T) heap[i], e) == 0){
				return remove(i);
			}
		}
		return null;
	}
	protected T remove(int i) {
		Object removed = heap[i];
		if(size == i) {
			heap[size] = null;
			size--;
		} else {
			T last = (T) heap[size];
			heap[size] = null;
			size--;
            downHeap(i, last);
            //새 원소가 움직이지 않았다면.
            if (heap[i] == last) {
                upHeap(i, last);
            }
		}
		return (T) removed;
	}
	public boolean replace(T oldEl, T newEl) {
		for (int i = 1; i <= size; i++) {
			if(compare((T) heap[i], oldEl) == 0){
				replaceEl(i, newEl);
				return true;
			}
		}
		
		return false;
	}
	protected T replaceEl(int i, T newEl) {
		Object removed = heap[i];
        downHeap(i, newEl);
        //새 원소가 움직이지 않았다면.
        if (heap[i] == newEl) {
            upHeap(i, newEl);
        }
		return (T) removed;
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
			logger.debug("{}", heap[i + 1]);
		}
	}
	private void upHeap() {
		int idx = size;
		Object node = heap[idx];
		upHeap(idx, node);
	}
	private void upHeap(int idx, Object node) {
		while(idx > 0) {
			int parent = idx >> 1;
			
			if(parent == 0 || compare((T) node, (T) heap[parent]) <= 0) {
				break;
			}
			heap[idx] = heap[parent];
			idx = parent;
		}
		heap[idx] = node;
		
	}
	private void downHeap(){
		int idx = 1;
		downHeap(idx, heap[idx]);
	}
	private void downHeap(int idx, Object node){
		
		int child = -1;
		
		//leaf 노드이면 down heap이 필요없다.
		int leafLimit = size >>> 1;
		while(idx <= leafLimit) {
			
			/*
			 * 1. 자식노드 두개중 더 큰걸 고른다.
			 * */
			int left = idx << 1;
			int right = left + 1;
			
			if(right <= size){
				if(compare((T) heap[left], (T) heap[right]) > 0) {
					child = left;
				} else {
					child = right;
				}
			}else{
				//if there is no right el.
				child = left;
			}
			
			/*
			 * 2. 자식노드중 큰 것과 나의 노드를 비교해서 자식이 더 작으면 종료.
			 * 내가 더 크면 바꿈.
			 * */
			if(compare((T) heap[child], (T) node) <= 0){
				//같거나 정렬되어 있으면 child를 확인하지 않는다.
				break;
			}
			
			heap[idx] = heap[child];
            idx = child;
		}
		
		heap[idx] = node;
	}
	
	public void printHeap(String label) {
		System.out.println("-- "+ label +" -------------");
		for(int i=1;i<=size;i++){
			System.out.print(heap[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
}
