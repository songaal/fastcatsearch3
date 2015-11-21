/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.index;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.*;
import org.fastcatsearch.ir.util.DocumentNumberConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 세그먼트내 Search index 를 머징하기 위한 포스팅 reader.
 * <p/>
 * 1. Lexicon. 키워드는 알피벳 오름차순 정렬.
 *    포맷 : int(텀갯수), { string(키워드), long(위치) }
 *
 * 2. Posting
 *    포맷 : int(필드옵션), { vInt(포스팅 데이터길이), int(갯수), int(마지막문서번호), { vInt(문서번호 delta), vInt(출현횟수), [ { vInt(위치 delta) } ] } }
 *
 * @see SearchIndexMerger
 */
public class SearchPostingReader {
    private static Logger logger = LoggerFactory.getLogger(SearchPostingReader.class);

    private int sequence;
    private String indexId;
    private CharVector term;
    private IndexInput lexiconInput;
    private IndexInput postingInput;
    private int termLeft;
    private int offset; //머징시 앞의 세그먼트이후로 문서번호를 부여해야 하므로, offset 를 사용한다.
    private IndexFieldOption indexFieldOption;

    private BitSet deleteSet;
    private int[] deleteIdList;
    private int aliveDocumentCount;
    private DocumentNumberConverter converter; //삭제문서를 제외하고 순차번호를 만들어주는 컨버터.

    private BytesBuffer currentBuffer; //현재선택된 버퍼
    private BytesBuffer[] bufferPool; //더블 버퍼링 재사용 버퍼.
    private int bufferPoolSelector; //재사용 버퍼 셀렉터.

    public SearchPostingReader(int sequence, String indexId, File dir) throws IOException {
        this(sequence, indexId, dir, 0);
    }
    public SearchPostingReader(int sequence, String indexId, File dir, int offset) throws IOException {
        this.sequence = sequence;
        this.indexId = indexId;
        this.offset = offset;

        lexiconInput = new BufferedFileInput(dir, IndexFileNames.getSearchLexiconFileName(indexId));
        postingInput = new BufferedFileInput(dir, IndexFileNames.getSearchPostingFileName(indexId));
        deleteSet = new BitSet(dir, IndexFileNames.docDeleteSet);
        IndexInput docInput = null;
        int documentCount = 0;
        try {
            docInput = new BufferedFileInput(dir, IndexFileNames.docStored);
            documentCount = docInput.readInt();
        } finally {
            if (docInput != null) {
                docInput.close();
            }
        }

        List<Integer> deleteList = new ArrayList<Integer>();
        for (int i = 0; i < documentCount; i++) {
            if (deleteSet.isSet(i)) {
                deleteList.add(i);
            }
        }
        deleteIdList = new int[deleteList.size()];
        for (int i = 0; i < deleteIdList.length; i++) {
            deleteIdList[i] = deleteList.get(i);
        }
        aliveDocumentCount = documentCount - deleteList.size();
        converter = new DocumentNumberConverter(deleteIdList);

        //색인된 키워드 갯수
        termLeft = lexiconInput.readInt();
        indexFieldOption = new IndexFieldOption(postingInput.readInt());

        bufferPool = new BytesBuffer[2];
        bufferPool[0] = new BytesBuffer(1024);
        bufferPool[1] = new BytesBuffer(1024);
        logger.debug("SearchPostingReader[{}:{}] >> terms[{}] doc[{}] deletes[{}] alive[{}]", indexId, sequence, termLeft, documentCount, deleteList.size(), aliveDocumentCount);
    }

    public IndexFieldOption indexFieldOption() {
        return indexFieldOption;
    }

    public int getAliveDocumentCount() {
        return aliveDocumentCount;
    }

    public void close() throws IOException {
        if (lexiconInput != null) {
            lexiconInput.close();
        }

        if (postingInput != null) {
            postingInput.close();
        }
    }

    public String indexId() {
        return indexId;
    }

    public int sequence() {
        return sequence;
    }

    public boolean nextTerm() throws IOException {
        if (termLeft == 0) {
            term = null;
            return false;
        }

        char[] array = null;
        long postingPos = -1;
        try {
            array = lexiconInput.readUString();
            postingPos = lexiconInput.readLong();
            postingInput.seek(postingPos);

        } catch (EOFException e) {
            logger.error(e.getMessage(), e);
            logger.debug("{} - count = {}", indexId, termLeft);
            throw e;
        }
        int len = array.length;
        term = new CharVector(array, 0, len);
//        int dataSize = postingInput.readVInt();
        /*
        * 2015.11.21 데이터길이를 고정길이 int로 바꾼다. 머징이 끝나고, 재기록하기 위함.
        * */
        int dataSize = postingInput.readInt();

        logger.debug("dataSize : {}, fileSize:{}, filePos:{}", dataSize, postingInput.length(), postingInput.position());
        currentBuffer = borrowBuffer(dataSize);
        postingInput.readBytes(currentBuffer);
//        docSize = postingInput.readInt();
//        lastDocNo = postingInput.readInt();
//        docPos = 0;
//        prevDocNo = -1;
//        prevDocPosition = -1;
        termLeft--;
        return true;
    }

    public SearchPostingBufferReader bufferReader() {
        return new SearchPostingBufferReader(sequence, currentBuffer, offset, deleteSet, deleteIdList, converter);
    }
    //재사용 버퍼는 얻는다.
    private BytesBuffer borrowBuffer(int minimumSize) {
        return new BytesBuffer(minimumSize);
//        bufferPoolSelector = (bufferPoolSelector + 1) % 2;
//        BytesBuffer buffer = bufferPool[bufferPoolSelector];
//        if(buffer.array().length < minimumSize) {
//            byte[] array = new byte[minimumSize];
//            buffer.init(array, 0, array.length);
//        }
//        buffer.pos(0);
//        buffer.limit = minimumSize;
//        return buffer;
    }
    public int left() {
        return termLeft;
    }

    public CharVector term() {
        return term;
    }

//    public int readDocPosition() throws IOException {
//        if(!isStorePosition) {
//            return -1;
//        }
//        if(prevDocPosition == -1) {
//            prevDocPosition = postingInput.readVInt();
//        } else {
//            prevDocPosition += (postingInput.readVInt() + 1);
//        }
//        return prevDocPosition;
//    }


//    public int readNewDocNo() throws IOException {
//        int docNo = readDocNo();
//        if(deleteSet.isSet(docNo)) {
//            //삭제됨.
//            return -1;
//        }
//        if(docNo != -1) {
//            return converter.convert(docNo) + offset;
//        }
//        return -1;
//    }
}
