package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionMergeIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 각 노드로 전달되어 색인머징을 수행하는 작업.
 * Created by swsong on 2015. 12. 24..
 */
public class NodeIndexMergingJob extends Job implements Streamable {

    protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

    private String collectionId;
    private String documentId;

    public NodeIndexMergingJob() {
    }

    public NodeIndexMergingJob(String collectionId, String documentId) {
        this.collectionId = collectionId;
        this.documentId = documentId;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        long startTime = System.currentTimeMillis();
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);

        // 머징 시작표시..
        if(collectionHandler.isMergingStatus()) {
            return new JobResult(false);
        }

        try {

            collectionHandler.startMergingStatus();

            CollectionContext collectionContext = collectionHandler.collectionContext();

            List<String> merge100 = new ArrayList<String>();
            List<String> merge10K = new ArrayList<String>();
            List<String> merge100K = new ArrayList<String>();
            List<String> merge1M = new ArrayList<String>();
            List<String> merge10M = new ArrayList<String>();
            List<String> mergeOver10M = new ArrayList<String>();

            // 세그먼트를 확인하여 머징가능한 조합을 찾아낸다.
            List<DataInfo.SegmentInfo> segmentInfoList = collectionContext.dataInfo().getSegmentInfoList();
            for (DataInfo.SegmentInfo segmentInfo : segmentInfoList) {
                int docSize = segmentInfo.getDocumentCount();
                int deleteSize = segmentInfo.getDeleteCount();
                String segmentId = segmentInfo.getId();

                //크기가 비슷한 것끼리 묶는다.
                //100, 1만, 10만, 100만, 1000만, 그이상 구간을 둔다

                if (docSize <= 100) {
                    merge100.add(segmentId);
                } else if (docSize <= 10 * 1000) {
                    merge10K.add(segmentId);
                } else if (docSize <= 100 * 1000) {
                    merge100K.add(segmentId);
                } else if (docSize <= 1000 * 1000) {
                    merge1M.add(segmentId);
                } else if (docSize <= 10 * 1000 * 1000) {
                    merge10M.add(segmentId);
                } else if (docSize > 10 * 1000 * 1000) {
                    mergeOver10M.add(segmentId);
                }

                //만약 삭제가 30% 이상이면 리스트에 segId를 2개 더 넣어주어서 최소 갯수이상이 되도록 맞춰준다.
                if(deleteSize >= docSize * 0.3f) {
                    if (docSize <= 100) {
                        merge100.add(segmentId);
                    } else if (docSize <= 10 * 1000) {
                        merge10K.add(segmentId);merge10K.add(segmentId);
                    } else if (docSize <= 100 * 1000) {
                        merge100K.add(segmentId);merge100K.add(segmentId);
                    } else if (docSize <= 1000 * 1000) {
                        merge1M.add(segmentId);merge1M.add(segmentId);
                    } else if (docSize <= 10 * 1000 * 1000) {
                        merge10M.add(segmentId);merge10M.add(segmentId);
                    } else if (docSize > 10 * 1000 * 1000) {
                        mergeOver10M.add(segmentId);mergeOver10M.add(segmentId);
                    }
                }
            }

            // 머징시 하위 구간을 모두 포함한다.
            Set<String> mergeSegmentIdList = new HashSet<String>();

            if (mergeOver10M.size() >= 3) {
                mergeSegmentIdList.addAll(mergeOver10M);
                mergeSegmentIdList.addAll(merge10M);
                mergeSegmentIdList.addAll(merge1M);
                mergeSegmentIdList.addAll(merge100K);
                mergeSegmentIdList.addAll(merge10K);
                mergeSegmentIdList.addAll(merge100);
            } else if (merge10M.size() >= 3) {
                mergeSegmentIdList.addAll(merge10M);
                mergeSegmentIdList.addAll(merge1M);
                mergeSegmentIdList.addAll(merge100K);
                mergeSegmentIdList.addAll(merge10K);
                mergeSegmentIdList.addAll(merge100);
            } else if (merge1M.size() >= 3) {
                mergeSegmentIdList.addAll(merge1M);
                mergeSegmentIdList.addAll(merge100K);
                mergeSegmentIdList.addAll(merge10K);
                mergeSegmentIdList.addAll(merge100);
            } else if (merge100K.size() >= 3) {
                mergeSegmentIdList.addAll(merge100K);
                mergeSegmentIdList.addAll(merge10K);
                mergeSegmentIdList.addAll(merge100);
            } else if (merge10K.size() >= 3) {
                mergeSegmentIdList.addAll(merge10K);
                mergeSegmentIdList.addAll(merge100);
            } else if (merge100.size() >= 2) {
                mergeSegmentIdList.addAll(merge100);
            }


            if (mergeSegmentIdList.size() >= 2) {
                logger.debug("---------------------");
                logger.debug("segmentInfoList = {}", segmentInfoList);
                logger.debug("[{}] Check Merging 100 > {}", collectionId, merge100);
                logger.debug("[{}] Check Merging 10k > {}", collectionId, merge10K);
                logger.debug("[{}] Check Merging 100k > {}", collectionId, merge100K);
                logger.debug("[{}] Check Merging 1M > {}", collectionId, merge1M);
                logger.debug("[{}] Check Merging 10M > {}", collectionId, merge10M);
                logger.debug("[{}] Check Merging Over10M > {}", collectionId, mergeOver10M);
                logger.debug("[{}] Check Merging Total > {}", collectionId, mergeSegmentIdList);
                logger.debug("---------------------");
                //mergeIdList 를 File[]로 변환.
                File[] segmentDirs = new File[mergeSegmentIdList.size()];
                int i = 0;
                for (String mergeSegmentId : mergeSegmentIdList) {
                    segmentDirs[i++] = collectionContext.indexFilePaths().segmentFile(mergeSegmentId);
                }
                CollectionMergeIndexer mergeIndexer = new CollectionMergeIndexer(documentId, collectionHandler, segmentDirs);
                DataInfo.SegmentInfo segmentInfo = null;
                Throwable indexingThrowable = null;
                try {
                    mergeIndexer.doIndexing();
                } catch (Throwable e) {
                    indexingThrowable = e;
                } finally {
                    if (mergeIndexer != null) {
                        try {
                            segmentInfo = mergeIndexer.close();
                        } catch (Throwable closeThrowable) {
                            // 이전에 이미 발생한 에러가 있다면 close 중에 발생한 에러보다 이전 에러를 throw한다.
                            if (indexingThrowable == null) {
                                indexingThrowable = closeThrowable;
                            }
                        }
                    }
                    if (indexingThrowable != null) {
                        throw indexingThrowable;
                    }
                }

                File segmentDir = mergeIndexer.getSegmentDir();
                if(segmentInfo.getDocumentCount() == 0 || segmentInfo.getLiveCount() <= 0) {
                    logger.info("[{}] Delete segment dir due to no documents = {}", collectionHandler.collectionId(), segmentDir.getAbsolutePath());
                    //세그먼트를 삭제하고 없던 일로 한다.
                    FileUtils.deleteDirectory(segmentDir);
                    collectionContext = collectionHandler.removeMergedSegment(mergeSegmentIdList);
                } else {
                    collectionContext = collectionHandler.applyMergedSegment(segmentInfo, mergeIndexer.getSegmentDir(), mergeSegmentIdList);
                }
                CollectionContextUtil.saveCollectionAfterDynamicIndexing(collectionContext);
                int totalLiveDocs = collectionContext.dataInfo().getDocuments() - collectionContext.dataInfo().getDeletes();
                long elapsed = System.currentTimeMillis() - startTime;
                indexingLogger.info("[{}] Merge Indexing Done. Inserts[{}] Deletes[{}] Elapsed[{}] TotalLive[{}] Segments[{}] SegIds{} ", collectionId, segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount(), Formatter.getFormatTime(elapsed), totalLiveDocs, mergeSegmentIdList.size(), mergeSegmentIdList);

                return new JobResult(true);
            } else {
                //머징없음.
                return new JobResult(false);
            }
        } catch (Throwable e) {
            logger.error("", e);
            throw new FastcatSearchException("ERR-00525", e);
        } finally {
            // 머징 끝남표시..
            collectionHandler.endMergingStatus();
        }
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
        documentId = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
        output.writeString(documentId);
    }

}
