package org.fastcatsearch.util;

import org.junit.Test;

import java.io.File;

/**
 * Created by swsong on 2016. 1. 13..
 */
public class TimeBaseRollingDocumentLoggerTest {

    @Test
    public void test1() {
        File dir = new File("/tmp/test");
        int flushDelay = 1; //1초
        int rollingPeriod = 10; //1초
        TimeBaseRollingDocumentLogger logger = new TimeBaseRollingDocumentLogger(dir, flushDelay, rollingPeriod);
        for (int i = 0; i < 1000000; i++) {
            String data = String.valueOf(i);
            logger.log(data);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
