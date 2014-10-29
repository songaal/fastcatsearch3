package org.fastcatsearch.ir.search;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by swsong on 2014. 10. 29..
 */
public class LargeHashSetTest {
    Random r = new Random(System.currentTimeMillis());

    //100만개 테스트.
    @Test
    public void test1M() {
        testCount(1000000);
    }

    //1000만개 테스트.
    @Test
    public void test10M() {
        testCount(10000000);
    }

    private void testCount(int count) {
        HashSet set = new HashSet();

        long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long st = System.currentTimeMillis();
        System.out.format("Mem1 : %,d b\n", mem1);
        for (int i = 0; i < count; i++) {
            byte[] data = generateData(32);
            set.add(data);
        }
        long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.format("Mem2 : %,d b\n", mem2);
        System.out.format("time : %sms, count : %d", System.currentTimeMillis() - st, count);
    }

    private byte[] generateData(int size) {
        byte[] data = new byte[size];
        r.nextBytes(data);
        return data;
    }


}
