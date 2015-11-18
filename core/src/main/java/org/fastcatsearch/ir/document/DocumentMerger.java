package org.fastcatsearch.ir.document;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 17..
 */
public class DocumentMerger {
    private static Logger logger = LoggerFactory.getLogger(DocumentMerger.class);

    private IndexOutput docOutput;
    private IndexOutput positionOutput;

    private int readerSize;
    private DocumentRawReader[] reader;

    public DocumentMerger(File dir) throws IOException {
        docOutput = new BufferedFileOutput(dir, IndexFileNames.docStored);
        positionOutput = new BufferedFileOutput(dir, IndexFileNames.docPosition);
    }

    public void merge(File... files) throws IOException {

        readerSize = files.length;
        if (readerSize <= 0) {
            return;
        }
        reader = new DocumentRawReader[readerSize];

        try {
            for (int i = 0; i < readerSize; i++) {
                reader[i] = new DocumentRawReader(files[i]);

                while(reader[i].read()) {
                    int docNo = reader[i].getDocNo();

                    if(reader[i].isAlive()) {
                        //기록.
                        byte[] buffer = reader[i].getBuffer();
                        int dataLength = reader[i].getDataLength();
                    } else {


                    }
                }

            }
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
