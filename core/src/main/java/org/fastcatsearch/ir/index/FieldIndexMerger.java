package org.fastcatsearch.ir.index;

import org.fastcatsearch.ir.document.DocumentRawReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 17..
 */
public class FieldIndexMerger {
    private static Logger logger = LoggerFactory.getLogger(FieldIndexMerger.class);

    private int readerSize;


    public FieldIndexMerger(File dir) {

    }
    public void merge(File... dirs) throws IOException {
        readerSize = dirs.length;
        if (readerSize <= 0) {
            return;
        }


        for (int i = 0; i < readerSize; i++) {
            if (isMultiValue) {
                reader = new DocumentRawReader[readerSize];
            } else {

            }

            while (reader[i].read()) {
                int docNo = reader[i].getDocNo();

                if (reader[i].isAlive()) {
                    //기록.
                    byte[] buffer = reader[i].getBuffer();
                    int dataLength = reader[i].getDataLength();
                    long docPosition = docOutput.position();
                    positionOutput.writeLong(docPosition);
                    docOutput.writeInt(dataLength);
                    docOutput.writeBytes(buffer, dataLength);
                    totalCount++;
                } else {
                    deleteCount++;
                }
            }
        }
        docOutput.seek(0);
        docOutput.writeInt(totalCount);
        logger.debug("Total Count[{}] Delete[{}]", totalCount, deleteCount);
    }

    public void close() {

    }
}
