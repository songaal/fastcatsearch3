package org.fastcatsearch.ir;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.job.indexing.LocalIndexMergingJob;
import org.fastcatsearch.job.indexing.NodeIndexMergingJob;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by swsong on 2016. 3. 4..
 */
public class IndexMergeScheduleWorker extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(IndexMergeScheduleWorker.class);

    private String collectionId;
    private boolean isCanceled;

    private long scheduleDelayInMS;

    private Map<String, String> mergingSegmentSet;
    private static float DELETE_ALLOW_RATIO = 0.4f;

    public void requestCancel() {
        this.interrupt();
        isCanceled = true;
    }

    public IndexMergeScheduleWorker(String collectionId, long scheduleDelayInMS) {
        super("IndexMergeScheduler-" + collectionId);
        setDaemon(true);
        this.collectionId = collectionId;
        this.scheduleDelayInMS = scheduleDelayInMS;
        mergingSegmentSet = new ConcurrentHashMap<String, String>();
    }

    private void putMerging(Set<String> segmentIdSet) {
        for(String segmentId : segmentIdSet) {
            mergingSegmentSet.put(segmentId, segmentId);
        }
    }

    private boolean isMerging(String segmentId) {
        return mergingSegmentSet.containsKey(segmentId);
    }

    public void finishMerging(Set<String> segmentIdSet) {
        for(String segmentId : segmentIdSet) {
            mergingSegmentSet.remove(segmentId);
        }
    }


    @Override
    public void run() {

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        while(!isCanceled) {
            try {
                Collection<SegmentReader> segmentReaders = collectionHandler.segmentReaders();
                List<String> merge100 = new ArrayList<String>();
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
                    if (isMerging(segmentId)) {
                        continue;
                    }

                    //크기가 비슷한 것끼리 묶는다.
                    //100, 1만, 10만, 100만, 1000만, 그이상 구간을 둔다
                    if (liveSize < 100) {
                        // 1~100
                        merge100.add(segmentId);
                    } else if (liveSize < 10 * 1000) {
                        // 100 ~ 1만
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

                if (mergeOver5M.size() > 0) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(mergeOver5M);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge5M.size() >= 3) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge5M);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge1M.size() >= 3) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge1M);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge100K.size() >= 3) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge100K);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge10K.size() >= 3) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge10K);
                    startMergingJob(mergeSegmentIdSet);
                }

                if (merge100.size() >= 3) {
                    Set<String> mergeSegmentIdSet = new HashSet<String>();
                    mergeSegmentIdSet.addAll(merge100);
                    startMergingJob(mergeSegmentIdSet);
                }

                Thread.sleep(scheduleDelayInMS);
            } catch (InterruptedException e) {
                //ignore
            } catch (Throwable t) {
                logger.error("", t);
            }
        }
    }

    private void startMergingJob(Set<String> mergeSegmentIdSet) {
        String documentId = String.valueOf(System.nanoTime());
        //끝나면 finish Merging()을 호출하도록 this를 전달한다.
        LocalIndexMergingJob mergingJob = new LocalIndexMergingJob(collectionId, documentId, mergeSegmentIdSet, this);
        mergingJob.setNoResult();
        putMerging(mergeSegmentIdSet);
        logger.debug("start merging job {} {} total merging on {}", collectionId, mergeSegmentIdSet, mergingSegmentSet);
        ServiceManager.getInstance().getService(JobService.class).offer(mergingJob);
    }
}
