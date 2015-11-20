package org.fastcatsearch.ir.index;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.util.DocumentNumberConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 20..
 */
public class SearchPostingBufferReader {
    private static Logger logger = LoggerFactory.getLogger(SearchPostingBufferReader.class);
    private BytesRef currentBuffer;
    private int offset;
    private BitSet deleteSet;
    private int[] deleteIdList;
    private DocumentNumberConverter converter;

    private int docPos; //읽은 문서갯수.
    private int docSize; //문서 갯수
    private int lastDocNo; //마지막 문서번호.
    private int prevDocNo; // 이전 문서번호. delta에 더하기 위해 필요.
//    private int frequency; //출현횟수.

    public SearchPostingBufferReader(BytesRef currentBuffer, int offset, BitSet deleteSet, int[] deleteIdList, DocumentNumberConverter converter) {
        this.currentBuffer = currentBuffer;
        this.offset = offset;
        this.deleteSet = deleteSet;
        this.deleteIdList = deleteIdList;
        this.converter = converter;

        docSize = IOUtil.readInt(currentBuffer);
        lastDocNo = IOUtil.readInt(currentBuffer);
    }

    public int docSize() {
        return docSize;
    }

    public int lastDocNo() {
        return lastDocNo;
    }

    public int readDocNo() throws IOException {
        if (docPos >= docSize) {
            return -1;
        }
        if(prevDocNo == -1) {
            prevDocNo = IOUtil.readVInt(currentBuffer);
        } else {
            prevDocNo += (IOUtil.readVInt(currentBuffer) + 1);
        }
//        frequency = IOUtil.readVInt(currentBuffer);
        docPos++;
        return prevDocNo;
    }

//    public int getFrequency() {
//        return frequency;
//    }

    //출현 횟수와 위치정보를 적는다. 버퍼끝까지 모두 적기.
    public void writePositionData(IndexOutput output) throws IOException {
        logger.debug("writePositionData pos={}, limit={}, rem={}", currentBuffer.pos(), currentBuffer.limit(), currentBuffer.remaining());
        output.writeBytes(currentBuffer);
    }

    public int getNewDocNo(int docNo) {
        return converter.convert(docNo) + offset;
    }
    /**
     * 문서 삭제여부 판단.
     */
    public boolean isAlive(int docNo) {
        return !deleteSet.isSet(docNo);
    }
}
