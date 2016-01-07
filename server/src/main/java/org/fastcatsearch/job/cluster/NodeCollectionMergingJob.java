package org.fastcatsearch.job.cluster;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.env.SettingManager;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionAddIndexer;
import org.fastcatsearch.ir.CollectionIndexerable;
import org.fastcatsearch.ir.CollectionMergeIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.indexing.IndexingStopException;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.util.CollectionContextUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 각 노드로 전달되어 색인머징을 수행하는 작업.
 * Created by swsong on 2015. 12. 24..
 */
public class NodeCollectionMergingJob extends Job implements Streamable {

    private String collectionId;

    public NodeCollectionMergingJob() {}


    @Override
    public JobResult doRun() throws FastcatSearchException {
        try {
            IRService irService = ServiceManager.getInstance().getService(IRService.class);
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
            CollectionContext collectionContext = collectionHandler.collectionContext();

            List<String> merge100 = new ArrayList<String>();
            List<String> merge10K = new ArrayList<String>();
            List<String> merge100K = new ArrayList<String>();
            List<String> merge1M = new ArrayList<String>();
            List<String> merge10M = new ArrayList<String>();
            List<String> mergeOver10M = new ArrayList<String>();

            // 세그먼트를 확인하여 머징가능한 조합을 찾아낸다.
            List<DataInfo.SegmentInfo> segmentInfoList = collectionContext.dataInfo().getSegmentInfoList();
            for(DataInfo.SegmentInfo segmentInfo : segmentInfoList) {
                int docSize = segmentInfo.getDocumentCount();
                String segmentId = segmentInfo.getId();

                //크기가 비슷한 것끼리 묶는다.
                //100, 1만, 10만, 100만, 1000만, 그이상 구간을 둔다
                // TODO 삭제문서까지 고려한 realSize기반으로 머징한다.
                if(docSize <= 100) {
                    merge100.add(segmentId);
                } else if(docSize <= 10 * 1000) {
                    merge10K.add(segmentId);
                } else if(docSize <= 100 * 1000) {
                    merge100K.add(segmentId);
                } else if(docSize <= 1000 * 1000) {
                    merge1M.add(segmentId);
                } else if(docSize <= 10 * 1000 * 1000) {
                    merge10M.add(segmentId);
                } else if(docSize > 10 * 1000 * 1000) {
                    mergeOver10M.add(segmentId);
                }
            }

            // 머징시 하위 구간을 모두 포함한다.
            List<String> mergeIdList = new ArrayList<String>();
            if(mergeOver10M.size() >= 2) {
                mergeIdList.addAll(mergeOver10M);
                mergeIdList.addAll(merge10M);
                mergeIdList.addAll(merge1M);
                mergeIdList.addAll(merge100K);
                mergeIdList.addAll(merge10K);
                mergeIdList.addAll(merge100);
            } else if(merge10M.size() >= 2) {
                mergeIdList.addAll(merge10M);
                mergeIdList.addAll(merge1M);
                mergeIdList.addAll(merge100K);
                mergeIdList.addAll(merge10K);
                mergeIdList.addAll(merge100);
            } else if(merge1M.size() >= 2) {
                mergeIdList.addAll(merge1M);
                mergeIdList.addAll(merge100K);
                mergeIdList.addAll(merge10K);
                mergeIdList.addAll(merge100);
            } else if(merge100K.size() >= 2) {
                mergeIdList.addAll(merge100K);
                mergeIdList.addAll(merge10K);
                mergeIdList.addAll(merge100);
            } else if(merge10K.size() >= 2) {
                mergeIdList.addAll(merge10K);
                mergeIdList.addAll(merge100);
            } else if(merge100.size() >= 2) {
                mergeIdList.addAll(merge100);
            }

            if(mergeIdList.size() >= 2) {
                ///머징한다.
                IndexingTaskState indexingTaskState = new IndexingTaskState(IndexingType.MERGE, true);

                //mergeIdList 를 File[]로 변환.
                File[] segmentDirs = new File[mergeIdList.size()];
                for (int i = 0; i < mergeIdList.size(); i++) {
                    segmentDirs[i] = collectionContext.indexFilePaths().segmentFile(mergeIdList.get(i));
                }
                CollectionMergeIndexer mergeIndexer = new CollectionMergeIndexer(collectionHandler, segmentDirs);
                boolean isIndexed = false;
                mergeIndexer.setTaskState(indexingTaskState);
                Throwable indexingThrowable = null;
                try {
                    mergeIndexer.doIndexing();
                }catch(Throwable e){
                    indexingThrowable = e;
                } finally {
                    if (mergeIndexer != null) {
                        try {
                            isIndexed = mergeIndexer.close();
                        } catch (Throwable closeThrowable) {
                            // 이전에 이미 발생한 에러가 있다면 close 중에 발생한 에러보다 이전 에러를 throw한다.
                            if (indexingThrowable == null) {
                                indexingThrowable = closeThrowable;
                            }
                        }
                    }
                    if(indexingThrowable != null){
                        throw indexingThrowable;
                    }
                }
                if(!isIndexed){
                    //여기서 끝낸다.
                    throw new IndexingStopException();
                }

                collectionHandler.addSegmentApplyCollection(mergeIndexer.getSegmentInfo(), mergeIndexer.getSegmentDir(), mergeIdList);

                CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
            } else {
                //머징없음.
            }

			/*
			 * 캐시 클리어.
			 */
            getJobExecutor().offer(new CacheServiceRestartJob());
            return new JobResult(true);

        } catch (Throwable e) {
            logger.error("", e);
            throw new FastcatSearchException("ERR-00525", e);
        }
    }
    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
    }

}
