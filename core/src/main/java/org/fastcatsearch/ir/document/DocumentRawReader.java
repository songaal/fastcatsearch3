package org.fastcatsearch.ir.document;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swsong on 2015. 11. 18..
 */
public class DocumentRawReader {
    private static Logger logger = LoggerFactory.getLogger(DocumentRawReader.class);
    private int lastDocNo;
    private int docNo;

    private IndexInput docInput;
    private IndexInput positionInput;
    private final BitSet deleteSet;

    private int documentCount;
    private long positionLimit;

    private byte[] buffer = new byte[8192];
    private int dataLength;

    private boolean isAlive;

    public DocumentRawReader(File dir) throws IOException {
        docInput = new BufferedFileInput(dir, IndexFileNames.docStored);
        positionInput = new BufferedFileInput(dir, IndexFileNames.docPosition);
        deleteSet = new BitSet(dir, IndexFileNames.docDeleteSet);
        positionLimit = positionInput.length();
        documentCount = docInput.readInt();
        logger.info("DocumentCount = {}", documentCount);
    }

    public boolean read() throws IOException {
        lastDocNo = -1;
        dataLength = 0;
        isAlive = false;

        if(docNo >= documentCount) {
            return false;
        }

        long positionOffset = docNo * IOUtil.SIZE_OF_LONG;
        if(deleteSet.isSet(docNo)) {
            isAlive = false;
        } else {
            positionInput.seek(positionOffset);
            long pos = positionInput.readLong();
            // find a document block
            docInput.seek(pos);
            dataLength = docInput.readInt();
            if (buffer.length < dataLength) {
                int newLen = buffer.length;
                while (newLen < dataLength) {
                    newLen *= 2;
                }
                buffer = new byte[newLen];
            }
            docInput.readBytes(buffer, 0, dataLength);
            isAlive = true;
        }
        lastDocNo = docNo;
        docNo++;
        return true;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getDataLength() {
        return dataLength;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getDocNo() {
        return lastDocNo;
    }

    public void close() throws IOException {
        IOException exception = null;
        try {
            if (docInput != null) {
                docInput.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        try {
            if (positionInput != null) {
                positionInput.close();
            }
        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) {
            throw exception;
        }
    }
}
