package org.fastcatsearch.ir.document;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * doc.position File
 *   format : {long(docStore position)}
 * doc.stored File
 *   format : int(docCount), {int(compress data length), byte[](one document)}
 *
 * Created by swsong on 2015. 11. 17..
 */
public class DocumentMerger {
    private static Logger logger = LoggerFactory.getLogger(DocumentMerger.class);

    private IndexOutput docOutput;
    private IndexOutput positionOutput;
    private PrimaryKeyIndexWriter primaryKeyIndexWriter;
    private int readerSize;
    private DocumentRawReader[] reader;

    public DocumentMerger(File dir) throws IOException {
        docOutput = new BufferedFileOutput(dir, IndexFileNames.docStored);
        positionOutput = new BufferedFileOutput(dir, IndexFileNames.docPosition);
//        primaryKeyIndexWriter = new PrimaryKeyIndexWriter();
    }

    public void merge(File... dirs) throws IOException {

        readerSize = dirs.length;
        if (readerSize <= 0) {
            return;
        }
        reader = new DocumentRawReader[readerSize];

        int totalCount = 0;
        int deleteCount = 0;
        docOutput.writeInt(totalCount);
        try {
            for (int i = 0; i < readerSize; i++) {

                reader[i] = new DocumentRawReader(dirs[i]);

                while(reader[i].read()) {
                    int docNo = reader[i].getDocNo();

                    if(reader[i].isAlive()) {
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
        } finally {
            //CLOSE
            IOException exception = null;
            for (int i = 0; i < readerSize; i++) {
                try {
                    reader[i].close();
                } catch (IOException e) {
                    exception = e;
                }
            }
            if (exception != null) {
                throw exception;
            }
        }


    }

    public void close() throws IOException {
        IOException exception = null;
        try {
            if (docOutput != null) {
                docOutput.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        try {
            if (positionOutput != null) {
                positionOutput.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        if (exception != null) {
            throw exception;
        }
    }
}
