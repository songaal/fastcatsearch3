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

package org.fastcatsearch.ir;

import java.util.Random;

import junit.framework.TestCase;

public class SortTest extends TestCase {
	public void testInsertionSort() {
		final int NUM = 10000000;
		Random r = new Random();
		int[] in = new int[NUM];
		int[] in2 = new int[NUM];
		for (int i = 0; i < NUM; i++) {
			in[i] = r.nextInt(NUM);
//			in[i] = i;
			in2[i] = in[i];
		}
		System.out.println("Start!");
		long st = System.currentTimeMillis();
//		 insertionSort(in,1,in.length-1);
//		 System.out.println("insertionSort = "+(System.currentTimeMillis() - st));

		st = System.currentTimeMillis();
		quickSort(in, 0, in.length - 1);
		System.out.println("quickSort = " + (System.currentTimeMillis() - st));

		
		st = System.currentTimeMillis();
		quickSortIterative(in2, 0, in2.length - 1);
		System.out.println("quickSortIterative = " + (System.currentTimeMillis() - st));
		
		
		int prev = -1;
		for (int i = 0; i < NUM; i++) {
			// System.out.println(in2[i]);
			if (in2[i] < prev) {
				System.out.println("ERROR!!");
				break;
			}
			prev = in2[i];
		}
		
		System.out.println("Done!");
	}

	void insertionSort(int[] list, int first, int last) {
		int i, j, c;

		for (i = first; i <= last; i++) {
			j = list[i];
			c = i;
			while ((list[c - 1] > j) && (c > first)) {
				list[c] = list[c - 1];
				c--;
			}
			list[c] = j;
		}
	}

	void quickSort(int[] numbers, int left, int right) {
		int pivot, l_hold, r_hold;
		l_hold = left;
		r_hold = right;
		pivot = numbers[left]; // 0번째 원소를 피봇으로 선택
		while (left < right) {
			// 값이 선택한 피봇과 같거나 크다면, 이동할 필요가 없다
			while ((numbers[right] >= pivot) && (left < right))
				right--;

			// 그렇지 않고 값이 피봇보다 작다면,
			// 피봇의 위치에 현재 값을 넣는다.
			if (left != right) {
				numbers[left] = numbers[right];
			}
			// 왼쪽부터 현재 위치까지 값을 읽어들이면서
			// 피봇보다 큰은값이 있다면, 값을 이동한다.
			while ((numbers[left] <= pivot) && (left < right))
				left++;
			if (left != right) {
				numbers[right] = numbers[left];
				right--;
			}
		}
		// 모든 스캔이 끝났다면, 피봇값을 현재 위치에 입력한다.
		// 이제 피봇을 기준으로 왼쪽에는 피봇보다 크거나 같은 값만 남았다.
		numbers[left] = pivot;
		pivot = left;
		left = l_hold;
		right = r_hold;

		// 재귀호출을 수행한다.
		if (left < pivot)
			quickSort(numbers, left, pivot - 1);
		if (right > pivot)
			quickSort(numbers, pivot + 1, right);
	}

	private void quickSortIterative(int[] ids, int first, int last) {
		if(last <= 0)
			return;
		
		int stackMaxSize = (int) ((Math.log(last) + 3) * 2);
		int[][] stack = new int[stackMaxSize][2];
		
		int pivotId = 0, sp = 0;
		int left = 0, right = 0;
		
		while(true){
			while(first < last){
			    left = first;
			    right = last;
			    int pivot = (left + right)/2;
				
				//move pivot to left most. 
				int tmp = ids[left];
				ids[left] = ids[pivot];
				ids[pivot] = tmp;
				pivotId = ids[left];
				
			    while (left < right) {
			    	while ((ids[right] - pivotId) >= 0 && (left < right)) 
			            right --;
			    	
			        if (left != right){ 
			             ids[left] = ids[right];
			             left++;
			        } 
			        
			        while ((ids[left] - pivotId) <= 0 && (left < right)) 
			            left ++;
			        
			        if (left != right) {
			             ids[right] = ids[left];
			             right --;
			        } 
			    } 
			    
			    ids[left] = pivotId;
			    
			    if(left - first < last - left){
				    if (left + 1 < last) {
				    	sp++;
				    	stack[sp][0] = left + 1;
						stack[sp][1] = last;
				    }
				    last = left - 1;
			    }else{
			    	if (first < left - 1) {
				    	sp++;
				    	stack[sp][0] = first;
						stack[sp][1] = left -1;
				    }
				    first = left + 1;
			    }
			    
			}
			
			if (sp == 0) {
				return;
			}else {
				first = stack[sp][0];
				last = stack[sp][1];
				sp--;
			}
			
		}
		
	} 
}
