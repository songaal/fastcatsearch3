package org.fastcatsearch.ir.document;

import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class CompareTest {

    private static Logger logger = LoggerFactory.getLogger(CompareTest.class);

    @Test
    public void testByteArraySort() {
        final int START = 1762510;
        final int SIZE = 100000;
        Random r = new Random(System.nanoTime());
        BytesBuffer[] list = new BytesBuffer[SIZE];
        for (int i = 0; i < SIZE; i++) {
//            int a = r.nextInt();
            int a = START + i;
            BytesBuffer buffer = new BytesBuffer(4);
            BytesBuffer buffer2 = new BytesBuffer(4);
            IOUtil.writeInt(buffer, a);
            IOUtil.writeInt(buffer2, a + 1);
            logger.debug("------");
            logger.debug("{} / {} ", a + 1, a);
            logger.debug("{} / {}", buffer2.array(), buffer.array());
            for (int j = 0; j < 4; j++) {
                int aa = buffer2.array()[j];
                int bb = buffer.array()[j];
                aa = buffer2.array()[j] & 0xFF;
                bb = buffer.array()[j] & 0xFF;
                assert (aa >= bb);
                if (aa > bb)
                    break;
            }
        }

    }


    @Test
    public void testIntBytes() {
        int a = -10000;
        int b = -a;

        logger.info("a={}, b={}, a < b = {}", a, b, (a < b));

        BytesBuffer buffer = new BytesBuffer(4);
        BytesBuffer buffer2 = new BytesBuffer(4);
        IOUtil.writeInt(buffer, a);
        IOUtil.writeInt(buffer2, b);
        buffer.flip();
        buffer2.flip();
        int comp = BytesBuffer.compareBuffer(buffer, buffer2);
        logger.info("a={}, b={}, a?b={}", buffer, buffer2, comp);

    }
}
