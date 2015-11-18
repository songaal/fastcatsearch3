package org.fastcatsearch.ir.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by swsong on 2015. 11. 18..
 */
public class DocumentNumberConverterTest {

    @Test
    public void test1() {
        int[] deleteIdList = new int[]{3, 5, 7, 9, 10};
        DocumentNumberConverter converter = new DocumentNumberConverter(deleteIdList);
        int LIMIT = 10;
        int seq = 0;
        for (int i = 0; i <= LIMIT; i++) {
            int newDocNo = converter.convert(i);
            if (newDocNo != -1) {
                System.out.println("# " + i + " > " + newDocNo);
                assertTrue(newDocNo == seq);
                seq++;
            }
        }
    }

    @Test
    public void test2() {
        int[] deleteIdList = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        DocumentNumberConverter converter = new DocumentNumberConverter(deleteIdList);
        int LIMIT = 10;
        int seq = 0;
        for (int i = 0; i <= LIMIT; i++) {
            int newDocNo = converter.convert(i);
            if (newDocNo != -1) {
                System.out.println("# " + i + " > " + newDocNo);
                assertTrue(newDocNo == seq);
                seq++;
            }
        }
    }

    @Test
    public void testRandom() {
        int LIMIT = 10000000;
        Random r = new Random(System.currentTimeMillis());

        List<Integer> deleteList = new ArrayList<Integer>();
        int docNo = 0;
        while(docNo < LIMIT) {
            int gap = r.nextInt(30) + 1;
            docNo += gap;
            if(docNo >= LIMIT) {
                break;
            }
            deleteList.add(docNo);
        }

        int[] deleteIdList = new int[deleteList.size()];
        for(int i = 0; i < deleteIdList.length; i++) {
            deleteIdList[i] = deleteList.get(i);
//            System.out.println(deleteIdList[i]);
        }
        System.out.println("=============================");
        System.out.println("Deletes : " + deleteIdList.length);
        DocumentNumberConverter converter = new DocumentNumberConverter(deleteIdList);

        int seq = 0;
        int deletes = 0;
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            int newDocNo = converter.convert(i);
            if (newDocNo != -1) {
//                System.out.println("# " + i + " > " + newDocNo);
                assertTrue(newDocNo == seq);
                seq++;
            } else {
//                System.out.println("X " + i);
                deletes++;
            }
        }
        System.out.println("Time : " + (System.nanoTime() - st) / 1000000 + "ms");
        assertTrue(deletes == deleteIdList.length);
    }
}
