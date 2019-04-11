package org.fastcatsearch.job.indexing;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionMergeIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 로컬 노드에서 색인머징을 수행하는 작업.
 */
public class LocalIndexMergingJob extends Job {

    protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

    private String collectionId;
    private String documentId;
    private Set<String> mergingSegmentIdSet;

    public LocalIndexMergingJob() {
    }

    public LocalIndexMergingJob(String collectionId, String documentId, Set<String> mergingSegmentIdSet) {
        this.collectionId = collectionId;
        this.documentId = documentId;
        this.mergingSegmentIdSet = mergingSegmentIdSet;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        long startTime = System.currentTimeMillis();
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);

        collectionHandler.prepareMergingDeletion(documentId);
        try {
            CollectionContext collectionContext = collectionHandler.collectionContext();
            //mergeIdList 를 File[]로 변환.
            List<File> segmentDirs = new ArrayList<File>();
            for (String mergeSegmentId : mergingSegmentIdSet) {
                SegmentReader segmentReader = collectionHandler.segmentReader(mergeSegmentId);
                if (segmentReader == null) {
                    continue;
                }
                if (segmentReader.segmentInfo().getLiveCount() > 0) {
                    segmentDirs.add(collectionContext.indexFilePaths().segmentFile(mergeSegmentId));
                }
            }

            DataInfo.SegmentInfo segmentInfo = null;

            if (segmentDirs.size() == 0) {
                collectionHandler.takeMergingDeletion(documentId);
                collectionContext = collectionHandler.removeZeroSegment(mergingSegmentIdSet);
                CollectionContextUtil.saveCollectionAfterDynamicIndexing(collectionContext);
                long elapsed = System.currentTimeMillis() - startTime;
                indexingLogger.info("[{}] Merge Indexing Done. Inserts[{}] Deletes[{}] Elapsed[{}] TotalLive[{}] Segments[{}] SegIds{} "
                        , collectionId, 0, 0, Formatter.getFormatTime(elapsed)
                        , 0, mergingSegmentIdSet.size(), mergingSegmentIdSet);
            } else {
                CollectionMergeIndexer mergeIndexer = new CollectionMergeIndexer(documentId, collectionHandler, segmentDirs.toArray(new File[0]));

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
                if (segmentInfo.getDocumentCount() == 0 || segmentInfo.getLiveCount() <= 0) {
                    indexingLogger.info("[{}] Delete segment dir due to no documents = {}", collectionHandler.collectionId(), segmentDir.getAbsolutePath());
                    //세그먼트를 삭제하고 없던 일로 한다.
                    FileUtils.deleteDirectory(segmentDir);
                    collectionHandler.takeMergingDeletion(documentId);
                    collectionContext = collectionHandler.removeZeroSegment(mergingSegmentIdSet);
                } else {
                    collectionContext = collectionHandler.applyMergedSegment(segmentInfo, mergeIndexer.getSegmentDir(), mergingSegmentIdSet);
                }
                CollectionContextUtil.saveCollectionAfterDynamicIndexing(collectionContext);
                int totalLiveDocs = collectionContext.dataInfo().getDocuments() - collectionContext.dataInfo().getDeletes();
                long elapsed = System.currentTimeMillis() - startTime;
                indexingLogger.info("[{}] Merge Indexing Done. Inserts[{}] Deletes[{}] Elapsed[{}] TotalLive[{}] Segments[{}] SegIds{} "
                        , collectionId, segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount(), Formatter.getFormatTime(elapsed)
                        , totalLiveDocs, mergingSegmentIdSet.size(), mergingSegmentIdSet);
            }

            return new JobResult(true);

        } catch (Throwable e) {
            logger.error("", e);
            throw new FastcatSearchException("ERR-00525", e);
        }
    }
}
