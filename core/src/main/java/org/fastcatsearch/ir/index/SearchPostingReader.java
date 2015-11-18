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
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexInput;
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
 * 포맷 : string(키워드), long(위치)
 * <p/>
 * 2. Posting
 * 포맷 : vInt(데이터길이), int(갯수), int(마지막문서번호), vInt(문서번호delta)
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

    private IndexFieldOption indexFieldOption;

    private int docPos; //읽은 문서갯수.
    private int docSize; //문서 갯수
    private int lastDocNo; //마지막 문서번호.
    private int prevDocNo; // 이전 문서번호. delta에 더하기 위해 필요.

    private BitSet deleteSet;
    private int[] deleteIdList;

    public SearchPostingReader(int sequence, String indexId, File dir) throws IOException {
        this.sequence = sequence;
        this.indexId = indexId;
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

        //색인된 키워드 갯수
        termLeft = lexiconInput.readInt();
        indexFieldOption = new IndexFieldOption(postingInput.readInt());
    }

    public IndexFieldOption indexFieldOption() {
        return indexFieldOption;
    }

    /**
     * 문서 삭제여부 판단.
     */
    public boolean isAlive(int docNo) {
        return !deleteSet.isSet(docNo);
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

            docSize = 0;
            lastDocNo = -1;

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
        int bufLength = postingInput.readVInt();
        docSize = postingInput.readInt();
        lastDocNo = postingInput.readInt();
        docPos = 0;
        prevDocNo = 0;
        termLeft--;
        return true;
    }

    public int left() {
        return termLeft;
    }

    public CharVector term() {
        return term;
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
        prevDocNo += postingInput.readVInt();
        docPos++;
        return prevDocNo;
    }
}
