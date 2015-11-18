package org.fastcatsearch.ir.document;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 18..
 */
public class DocumentRawReaderTest {
    private static Logger logger = LoggerFactory.getLogger(DocumentRawReaderTest.class);

    @Test
    public void read() throws IOException {
        File dir = new File("/Users/swsong/TEST_HOME/fastcatsearch-2.21.6/collections/volm/data/index0/a0");
//        File dir = new File("/tmp/doc_merge");
        DocumentRawReader reader = null;
        try {
            reader = new DocumentRawReader(dir);
            int alive = 0;
            int delete = 0;
            while (reader.read()) {
                int docNo = reader.getDocNo();
                if (reader.isAlive()) {
                    //기록.
                    byte[] buffer = reader.getBuffer();
                    int dataLength = reader.getDataLength();
                    logger.debug("{} len[{}]", docNo, dataLength);
                    alive++;
                } else {
                    logger.debug("X {}", docNo);
                    delete++;
                }
            }
            logger.debug(">> Alive[{}] Delete[{}]", alive, delete);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

    }
}
