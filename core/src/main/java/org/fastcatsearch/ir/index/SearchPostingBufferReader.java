package org.fastcatsearch.ir.index;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.util.DocumentNumberConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 하나의 term에 대해 문서번호, 출현횟수, 출현위치정보를 계속해서 읽어나갈수 있는 클래스.
 * Created by swsong on 2015. 11. 20..
 */
public class SearchPostingBufferReader {
    private static Logger logger = LoggerFactory.getLogger(SearchPostingBufferReader.class);
    private int sequence;
    private BytesBuffer currentBuffer;
    private int offset;
    private BitSet deleteSet;
    private int[] deleteIdList;
    private DocumentNumberConverter converter;

    private int docPos; //읽은 문서갯수.
    private int docSize; //문서 갯수
    private int lastDocNo; //마지막 문서번호.
    private int prevDocNo; // 이전 문서번호. delta에 더하기 위해 필요.
    private int frequency; //출현횟수.

    public SearchPostingBufferReader(int sequence, BytesBuffer currentBuffer, int offset, BitSet deleteSet, int[] deleteIdList, DocumentNumberConverter converter) {
        this.sequence = sequence;
        this.currentBuffer = currentBuffer;
        this.offset = offset;
        this.deleteSet = deleteSet;
        this.deleteIdList = deleteIdList;
        this.converter = converter;
        this.prevDocNo = -1;
        this.frequency = 0;
        docSize = IOUtil.readInt(currentBuffer);
        lastDocNo = IOUtil.readInt(currentBuffer);
        logger.debug("SearchPostingBufferReader:{}:init dataSize={}, pos={}, limit={}", sequence, currentBuffer.remaining(), currentBuffer.pos(), currentBuffer.limit);
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
        if (prevDocNo == -1) {
            prevDocNo = IOUtil.readVInt(currentBuffer);
        } else {
            int delta = IOUtil.readVInt(currentBuffer);
            logger.debug("delta > {}", delta);
            prevDocNo += (delta + 1);
        }
        frequency = IOUtil.readVInt(currentBuffer);
        logger.debug("readDocNo {} >> {}) {}", sequence, docPos, prevDocNo);
        logger.debug("readDocNo:{}:done dataSize={}, pos={}, limit={}", sequence, currentBuffer.remaining(), currentBuffer.pos(), currentBuffer.limit);
        docPos++;
        return prevDocNo;
    }

    public int getFrequency() {
        return frequency;
    }

    //출현 횟수와 위치정보를 적는다. 버퍼끝까지 모두 적기.
    public void readPositionData(IndexOutput output) throws IOException {
        logger.debug("readPositionData:{}:start dataSize={}, pos={}, limit={}", sequence, currentBuffer.remaining(), currentBuffer.pos(), currentBuffer.limit);
        for (int i = 0; i < frequency; i++) {
            output.writeVInt(IOUtil.readVInt(currentBuffer));
        }
        logger.debug("readPositionData:{}:done dataSize={}, pos={}, limit={}", sequence, currentBuffer.remaining(), currentBuffer.pos(), currentBuffer.limit);
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
