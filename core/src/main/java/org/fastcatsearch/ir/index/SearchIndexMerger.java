package org.fastcatsearch.ir.index;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 1. Lexicon. 키워드는 알피벳 오름차순 정렬.
 *    포맷 : int(텀갯수), { string(키워드), long(위치) }
 *
 * 2. Posting
 *    포맷 : int(필드옵션), { int(포스팅 데이터길이), int(문서갯수), int(마지막문서번호), { vInt(문서번호 delta), vInt(출현횟수), [ { vInt(위치 delta) } ] } }
 *
 * 3. Index
 *    포맷 : int(텀갯수), { string(색인키워드), long(가까운키워드위치), long(포스팅위치) }
 *
 * @see SearchPostingReader
 * Created by swsong on 2015. 11. 17..
 */
public class SearchIndexMerger {
    private static Logger logger = LoggerFactory.getLogger(SearchIndexMerger.class);

    private final String indexId;
    private int indexInterval;
    private IndexOutput postingOutput;
    private IndexOutput lexiconOutput;
    private IndexOutput indexOutput;

    protected int[] heap;
    private int readerSize;
    private SearchPostingReader[] reader;
    private SearchPostingBufferReader[] workingReaders;
    private int workingReaderSize;
    /**
     * 각 세그먼트의 indexId 를 머징한다. 출력 디렉토리는 dir.
     */
    public SearchIndexMerger(String indexId, File dir, int indexInterval) throws IOException {
        this.indexId = indexId;
        this.indexInterval = indexInterval;
        lexiconOutput = new BufferedFileOutput(dir, IndexFileNames.getSearchLexiconFileName(indexId));
        postingOutput = new BufferedFileOutput(dir, IndexFileNames.getSearchPostingFileName(indexId));
        indexOutput = new BufferedFileOutput(dir, IndexFileNames.getSearchIndexFileName(indexId));
    }

    /**
     * 머징 대상이 되는 세그먼트 디렉토리들
     */
    public void merge(File... dirs) throws IOException {
        readerSize = dirs.length;
        if (readerSize <= 0) {
            return;
        }

        reader = new SearchPostingReader[readerSize];
        workingReaders = new SearchPostingBufferReader[readerSize];
        int prevSegmentAliveDocumentCount = 0;
        for (int i = 0; i < readerSize; i++) {
            //여러 세그먼트를 순차적으로 머징시 이전 세그먼트의 문서갯수이후로 새 문서번호를 부여받을 것이므로 prevSegmentAliveDocumentCount를 알아야 한다.
            reader[i] = new SearchPostingReader(i, indexId, dirs[i], prevSegmentAliveDocumentCount);
            reader[i].nextTerm();
            prevSegmentAliveDocumentCount += reader[i].getAliveDocumentCount();
        }

        IndexFieldOption fieldIndexOption = reader[0].indexFieldOption();
        boolean isStorePosition = fieldIndexOption.isStorePosition();
        postingOutput.writeInt(fieldIndexOption.value());

        makeHeap(readerSize);

        int termCount = 0;
        int indexTermCount = 0;

        lexiconOutput.writeInt(termCount);// termCount
        indexOutput.writeInt(indexTermCount);// indexTermCount


        CharVector cv = null;
        CharVector cvOld = null;
        CharVector term = new CharVector();
        long postingBeforePosition = -1;

        int totalCount = 0;
        int prevDocNo = -1;
        while (true) {
            boolean termMade = false;
            int idx = heap[1];
            cv = reader[idx].term();
            if (cv == null && cvOld == null) {
                // if cv and cvOld are null, it's done
                break;
            }
            // cv == null일경우는 모든 reader가 종료되어 null이 된경우이며
            // cvOld 와 cv 가 다른 경우는 머징시 텀이 바뀐경우. cvOld를 기록해야한다.
            logger.debug("cv[{}] old[{}]", cv, cvOld);
            if ((cv == null || !cv.equals(cvOld)) && cvOld != null) {
                // merge workingReaders
                postingBeforePosition = postingOutput.position();
                //1. data Size
                postingOutput.writeInt(0);
                //2. 문서갯수
                postingOutput.writeInt(0);
                //3. last doc no
                postingOutput.writeInt(0);

                prevDocNo = -1;
                totalCount = 0;
                for (int k = 0; k < workingReaderSize; k++) {
                    SearchPostingBufferReader reader = workingReaders[k];
                    for (int i = 0; i < reader.docSize(); i++) {
                        int docNo = reader.readDocNo();
                        /*
                        * 여기서 실제 삭제가 이루어 진다.
                        * */
                        if (reader.isAlive(docNo)) {
                            //삭제문서가 적용된 새로운 문서번호가 리턴된다.
                            docNo = reader.getNewDocNo(docNo);
                            if (prevDocNo >= 0) {
                                postingOutput.writeVInt(docNo - prevDocNo - 1);
                            } else {
                                postingOutput.writeVInt(docNo);
                            }
                            postingOutput.writeVInt(reader.getFrequency());
                            //출현횟수와 출현위치(isStorePosition일때만)를 기록한다.
                            if(isStorePosition) {
                                reader.readPositionData(postingOutput);
                            }
                            //기록한 문서번호만 prevDocNo로 셋팅해야 정확한 delta가 계산된다.
                            prevDocNo = docNo;
                            totalCount++;
                        }

                    }
                }

                termMade = true;
                term.init(cvOld.array(), cvOld.start(), cvOld.length());

                workingReaderSize = 0;
            }

            if (workingReaderSize < workingReaders.length) {
                try {
                    workingReaders[workingReaderSize++] = reader[idx].bufferReader();
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.error("### workingReaderSize= {}, workingReaders.len={}, idx={}, reader={}", workingReaderSize, workingReaders.length, idx, reader.length);
                }
            } else {
                logger.warn("wrong! {}", cv);
                logger.debug("### workingReaderSize= {}, workingReaders.len={}, idx={}, reader={}", workingReaderSize, workingReaders.length, idx, reader.length);
            }
            // backup cv to old
            cvOld = cv;

            reader[idx].nextTerm();

            heapify(1, readerSize);

            if (termMade) {
                //1. Write Posting
                long postingCurrentPosition = postingOutput.position();
                int dataLength = (int) (postingCurrentPosition - postingBeforePosition - IOUtil.SIZE_OF_INT); //data Size 기록은 뺀다.
                postingOutput.seek(postingBeforePosition);
                postingOutput.writeInt(dataLength);
                postingOutput.writeInt(totalCount);
                postingOutput.writeInt(prevDocNo);
                postingOutput.seek(postingCurrentPosition);

                //2. Write Lexicon
                long lexiconPosition = lexiconOutput.position();
                lexiconOutput.writeUString(term.array(), term.start(), term.length());
                lexiconOutput.writeLong(postingBeforePosition);

                //3. Write Index
                if (indexInterval > 0 && (termCount % indexInterval) == 0) {
                    indexOutput.writeUString(term.array(), term.start(), term.length());
                    indexOutput.writeLong(lexiconPosition);
                    indexOutput.writeLong(postingBeforePosition);
                    indexTermCount++;
                }

                termCount++;
                logger.debug("Write Term-{} : {}", termCount, new String(term.array(), term.start(), term.length()));
            }
        } // while(true)

        if (termCount > 0) {
            lexiconOutput.seek(0);
            lexiconOutput.writeInt(termCount);
            indexOutput.seek(0);
            indexOutput.writeInt(indexTermCount);
        } else {
            // 이미 indexTermCount는 0으로 셋팅되어 있으므로 기록할 필요없음.
        }
        logger.debug("## write index [{}] terms[{}] indexTerms[{}] indexInterval[{}]", indexId, termCount, indexTermCount, indexInterval);

        lexiconOutput.flush();
        indexOutput.flush();
        postingOutput.flush();

    }

    public void close() throws IOException {
        IOException exception = null;
        try {

            if (postingOutput != null) {
                postingOutput.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        try {
            if (lexiconOutput != null) {
                lexiconOutput.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        try {
            if (indexOutput != null) {
                indexOutput.close();
            }
        } catch (IOException e) {
            exception = e;
        }

        for (int i = 0; i < readerSize; i++) {
            if (reader[i] != null) {
                try {
                    reader[i].close();
                } catch (IOException e) {
                    exception = e;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    protected void makeHeap(int heapSize) {
        heap = new int[heapSize + 1];
        // index starts from 1
        for (int i = 0; i < heapSize; i++) {
            heap[i + 1] = i;
        }

        int n = heapSize >> 1; // last inner node index

        for (int i = n; i > 0; i--) {
            heapify(i, heapSize);
        }

    }

    protected void heapify(int idx, int heapSize) {

        int temp = -1;
        int child = -1;

        while (idx <= heapSize) {
            int left = idx << 1;// *=2
            int right = left + 1;

            if (left <= heapSize) {
                if (right <= heapSize) {
                    // 키워드가 동일할 경우 먼저 flush된 reader가 우선해야, docNo가 오름차순 정렬순서대로 올바로 기록됨.
                    // flush후 머징시 문제가 생기는 버그 해결됨 2013-5-21 swsong
                    int c = compareKey(left, right);
                    if (c < 0) {
                        child = left;
                    } else if (c > 0) {
                        child = right;
                    } else {
                        // 하위 value 둘이 같아서 seq확인.
                        // 같다면 id가 작은게 우선.
                        int a = heap[left];
                        int b = heap[right];
                        if (reader[a].sequence() < reader[b].sequence()) {
                            child = left;
                        } else {
                            child = right;
                        }
                    }
                } else {
                    // if there is no right el.
                    child = left;
                }
            } else {
                // no children
                break;
            }

            // compare and swap
            int c = compareKey(child, idx);
            if (c < 0) {
                temp = heap[child];
                heap[child] = heap[idx];
                heap[idx] = temp;
                idx = child;
                // System.out.println("idx1="+idx);
            } else if (c == 0) {
                // 하위와 자신의 value가 같아서 seq확인
                // 같다면 seq가 작은게 우선.
                int a = heap[idx];
                int b = heap[child];
                if (reader[a].sequence() > reader[b].sequence()) {
                    // 하위의 seq가 작아서 child채택!
                    temp = heap[child];
                    heap[child] = heap[idx];
                    heap[idx] = temp;
                    idx = child;
                } else {
                    // 내것을 그대로 사용.
                    // sorted
                    break;
                }
            } else {
                // sorted, then do not check child
                break;
            }

        }
    }

    protected int compareKey(int one, int another) {

        int a = heap[one];
        int b = heap[another];

        return compareKey(reader[a].term(), reader[b].term());
    }

    protected int compareKey(CharVector term1, CharVector term2) {

        // reader gets EOS, returns null
        if (term1 == null && term2 == null) {
            return 0;
        } else if (term1 == null)
            return 1;
        else if (term2 == null)
            return -1;

        int len = (term1.length() < term2.length()) ? term1.length() : term2.length();

        for (int i = 0; i < len; i++) {
            if (term1.charAt(i) != term2.charAt(i))
                return term1.charAt(i) - term2.charAt(i);
        }

        return term1.length() - term2.length();
    }

}
