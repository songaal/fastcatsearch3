package org.fastcatsearch.ir;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.job.indexing.LocalDocZeroDeleteJob;
import org.fastcatsearch.job.indexing.LocalIndexMergingJob;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by swsong on 2016. 3. 4..
 */
public class IndexMergeScheduleWorker extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(IndexMergeScheduleWorker.class);

    private String collectionId;
    private boolean isCanceled;

    private long scheduleDelayInMS;

    private static float DELETE_ALLOW_RATIO = 0.4f;

    private static final int MERGE_MIN_SIZE = 4;

    private CollectionHandler collectionHandler;

    public void requestCancel() {
        this.interrupt();
        isCanceled = true;
    }

    public IndexMergeScheduleWorker(String collectionId, long scheduleDelayInMS) {
        super("IndexMergeScheduler-" + collectionId);
        setDaemon(true);
        this.collectionId = collectionId;
        this.scheduleDelayInMS = scheduleDelayInMS;
    }

    @Override
    public void run() {

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        collectionHandler = irService.collectionHandler(collectionId);
        while(!isCanceled) {
            try {
                logger.trace("[{}] Check merging....", collectionId);
                Collection<SegmentReader> segmentReaders = collectionHandler.segmentReaders();
                List<String> zeroDocs = new ArrayList<String>();
                List<String> merge100 = new ArrayList<String>();
                List<String> merge1K = new ArrayList<String>();
                List<String> merge10K = new ArrayList<String>();
                List<String> merge100K = new ArrayList<String>();
                List<String> merge1M = new ArrayList<String>();
                List<String> merge5M = new ArrayList<String>();
                List<String> mergeOver5M = new ArrayList<String>();

                for(SegmentReader segmentReader : segmentReaders) {
                    DataInfo.SegmentInfo segmentInfo = segmentReader.segmentInfo();
                    int docSize = segmentInfo.getDocumentCount();
                    int deleteSize = segmentInfo.getDeleteCount();
                    int liveSize = docSize - deleteSize;
                    String segmentId = segmentInfo.getId();

                    //이미 머징중인 세그먼트라면 통과한다.
                    if (collectionHandler.isMerging(segmentId)) {
                        continue;
                    }

                    //크기가 비슷한 것끼리 묶는다.
                    //100, 1만, 10만, 100만, 1000만, 그이상 구간을 둔다
                    //머징은 문서갯수가 0보다 큰넘만..
                    if(liveSize <= 0) {
                        zeroDocs.add(segmentId);
                    } else {
                        if (liveSize < 100) {
                            // 1~100
                            merge100.add(segmentId);
                        } else if (liveSize < 1000) {
                            // 100 ~ 1000
                            merge1K.add(segmentId);
                        } else if (liveSize < 10 * 1000) {
                            // 1000 ~ 1만
                            merge10K.add(segmentId);
                        } else if (liveSize < 100 * 1000) {
                            // 1만 ~ 10만
                            merge100K.add(segmentId);
                        } else if (liveSize < 1000 * 1000) {
                            // 10만 ~ 100만
                            merge1M.add(segmentId);
                        } else if (liveSize < 5 * 1000 * 1000) {
                            // 100만 ~ 500만
                            merge5M.add(segmentId);
                        } else if (liveSize >= 5 * 1000 * 1000) {
                            // 500만 이상
                            if (deleteSize >= docSize * DELETE_ALLOW_RATIO) {
                                //삭제가 40%이상일때만 머징.
                                mergeOver5M.add(segmentId);
                            }
                        }
                    }
                }
                logger.trace("[{}] Check merging start....", collectionId);
                if(zeroDocs.size() > 0) {
                    Set<String> zeroSegmentIdSet = new HashSet<String>();
                    zeroSegmentIdSet.addAll(zeroDocs);
                    startRemoveJob(zeroSegmentIdSet);
                }
                if (mergeOver5M.size() > 0) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(mergeOver5M);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge5M.size() >= MERGE_MIN_SIZE) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge5M);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge1M.size() >= MERGE_MIN_SIZE) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge1M);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge100K.size() >= MERGE_MIN_SIZE) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge100K);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge10K.size() >= MERGE_MIN_SIZE) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge10K);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge1K.size() >= MERGE_MIN_SIZE) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge1K);
                    //100도 함께 추가한다.
                    mergeSegmentIdSet.addAll(merge100);
                    startMergingJob(mergeSegmentIdSet);
                } else if (merge100.size() >= MERGE_MIN_SIZE) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge100);
                    startMergingJob(mergeSegmentIdSet);
                }
                logger.trace("[{}] Check merging end....", collectionId);
                Thread.sleep(scheduleDelayInMS);
            } catch (InterruptedException e) {
                //ignore
            } catch (Throwable t) {
                logger.error("", t);
            }
        }
    }

    private void startRemoveJob(Set<String> zeroSegmentIdSet) {
        LocalDocZeroDeleteJob deleteJob = new LocalDocZeroDeleteJob(collectionId, zeroSegmentIdSet);
        deleteJob.setNoResult();
        logger.debug("[{}] start remove segment job {}", collectionId, deleteJob);
        ServiceManager.getInstance().getService(JobService.class).offer(deleteJob);
    }

    private void startMergingJob(Set<String> mergeSegmentIdSet) {
        String documentId = String.valueOf(System.nanoTime());
        LocalIndexMergingJob mergingJob = new LocalIndexMergingJob(collectionId, documentId, mergeSegmentIdSet);
        mergingJob.setNoResult();
        collectionHandler.putMerging(mergeSegmentIdSet);
        logger.debug("[{}] start merging job {}", collectionId, mergeSegmentIdSet);
        ServiceManager.getInstance().getService(JobService.class).offer(mergingJob);
    }
}
