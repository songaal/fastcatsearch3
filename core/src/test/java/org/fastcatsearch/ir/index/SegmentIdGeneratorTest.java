package org.fastcatsearch.ir.index;

import org.junit.Test;

/**
 * Created by swsong on 2015. 11. 16..
 */
public class SegmentIdGeneratorTest {

    @Test
    public void make() {
        int SIZE = 937;
        SegmentIdGenerator gen = new SegmentIdGenerator();
        for(int i = 0; i< SIZE; i++) {
            System.out.println(gen.nextId());
        }
    }
}
