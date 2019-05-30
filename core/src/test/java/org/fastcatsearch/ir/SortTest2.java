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

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

public class SortTest2 extends TestCase {


    int COUNT;
    int[] ids;
    int[] keys;

    public void test1() throws IOException {

        COUNT = 1000000;
        ids = new int[COUNT];
        keys = new int[COUNT];
//		keys = new int[]{3,6,5,2,1,100,90,10,20,30};
        Random r = new Random();
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i;
            int id = ids[i];
            keys[id] = r.nextInt(COUNT * 2);
        }
        long st = System.currentTimeMillis();
        boolean isAsc = true;
        int start = COUNT - 10;
        quickSort(ids, start, COUNT - 1);
        int prev = -1;
        for (int i = start; i < COUNT - 1; i++) {
            int id = ids[i];

            System.out.println(i + " = " + keys[id]);
            if (prev >= 0) {
                if (isAsc)
                    assertTrue(keys[id] >= prev);
                else
                    assertTrue(keys[id] <= prev);
            }
            prev = keys[id];
        }
        System.out.println("compare = " + c + ", t = " + (System.currentTimeMillis() - st) + ", mem = " + Runtime.getRuntime().totalMemory());
    }


    int c = 0;

    private void quickSort(int[] ids, int first, int last) {
        if (last <= 0)
            return;

        int stackMaxSize = (int) ((Math.log(last - first + 1) + 3) * 2);
        System.out.println("stackMaxSize = " + stackMaxSize);
        int[][] stack = new int[stackMaxSize][2];

        int pivotId = 0, sp = 0;
        int left = 0, right = 0;

        while (true) {
            while (first < last) {
                left = first;
                right = last;
                int pivot = (left + right) / 2;

                //move pivot to left most.
                int tmp = ids[left];
                ids[left] = ids[pivot];
                ids[pivot] = tmp;
                pivotId = ids[left];

                while (left < right) {
                    while (compareKey(ids[right], pivotId) >= 0 && (left < right))
                        right--;

                    if (left != right) {
                        ids[left] = ids[right];
                        left++;
                    }

                    while (compareKey(ids[left], pivotId) <= 0 && (left < right))
                        left++;

                    if (left != right) {
                        ids[right] = ids[left];
                        right--;
                    }
                }

                ids[left] = pivotId;

                if (left - first < last - left) {
                    if (left + 1 < last) {
                        sp++;
                        stack[sp][0] = left + 1;
                        stack[sp][1] = last;
                    }
                    last = left - 1;
                } else {
                    if (first < left - 1) {
                        sp++;
                        stack[sp][0] = first;
                        stack[sp][1] = left - 1;
                    }
                    first = left + 1;
                }

            }

            if (sp == 0) {
                return;
            } else {
                first = stack[sp][0];
                last = stack[sp][1];
                sp--;
            }

        }

    }

    private void quickSort2(int[] ids, int left, int right) {

        int pivot;
        int pivot_hold, l_hold, r_hold;
        l_hold = left;
        r_hold = right;
        pivot = ids[left];// 0번째 원소를 피봇으로 선택

        while (left < right) {
            // 값이 선택한 피봇과 같거나 크다면, 이동할 필요가 없다
            while (ids[right] >= pivot && (left < right))
                right--;
            // 그렇지 않고 값이 피봇보다 작다면,
            // 피봇의 위치에 현재 값을 넣는다.
            if (left != right) {
                ids[left] = ids[right];
                left++;
            }
            // 왼쪽부터 현재 위치까지 값을 읽어들이면서
            // 피봇보다 큰은값이 있다면, 값을 이동한다.
            while (ids[left] <= pivot && (left < right))
                left++;
            if (left != right) {
                ids[right] = ids[left];
                right--;
            }
        }
        // 모든 스캔이 끝났다면, 피봇값을 현재 위치에 입력한다.
        // 이제 피봇을 기준으로 왼쪽에는 피봇보다 크거나 같은 값만 남았다.
        ids[left] = pivot;
        pivot_hold = left;
        left = l_hold;
        right = r_hold;

        // 재귀호출을 수행한다.
        if (left < pivot_hold)
            quickSort(ids, left, pivot_hold - 1);
        if (right > pivot_hold)
            quickSort(ids, pivot_hold + 1, right);
    }

    private int compareKey(int id, int id2) {
        c++;
        return keys[id] - keys[id2];
    }

    public void testSortArray() {
        int m = 0;
        Integer[] termDocList = new Integer[10];
        termDocList[m++] = 3;
        termDocList[m++] = 6;
        termDocList[m++] = 5;
        termDocList[m++] = 2;
//        Arrays.sort(termDocList);
		Arrays.sort(termDocList, 0, m);
        for (int i = 0; i < termDocList.length; i++) {
            System.out.println(termDocList[i]);
        }
    }


}
