package org.fastcatsearch.job.cluster;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.util.CollectionContextUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 각 노드로 전달되어 색인머징을 수행하는 작업.
 * Created by swsong on 2015. 12. 24..
 */
public class NodeCollectionMergingJob extends Job implements Streamable {

    private CollectionContext collectionContext;

    public NodeCollectionMergingJob() {}


    @Override
    public JobResult doRun() throws FastcatSearchException {
        try {

            String collectionId = collectionContext.collectionId();


            //TODO
            // 세그먼트를 확인하여 머징가능한 조합을 찾아낸다.
            List<DataInfo.SegmentInfo> segmentInfoList = collectionContext.dataInfo().getSegmentInfoList();
            for(DataInfo.SegmentInfo segmentInfo : segmentInfoList) {
                int docSize = segmentInfo.getDocumentCount();

                //TODO
                //크기가 비슷한 것끼리 묶는다.
                //100, 1만, 10만, 100만, 1000만, 그이상 구간을 둔다
                // 삭제문서까지 고려한 realSize기반으로 머징한다.

                //TODO
                // 머징시 하위 구간을 모두 포함한다.
            }


            //TODO
            //삭제처리는 세그먼트를 apply할때 발생하므로,
            //머징중 새로운세그먼트가 붙여질수 있으므로,
            //머징이 끝나고 붙이기 직전 해당 세그먼트에 삭제문서가 추가되었는지 확인하여
            //머징세그먼트에 삭제처리를 추가로 수행한다.





            DataInfo.SegmentInfo segmentInfo = collectionContext.dataInfo().getLastSegmentInfo();

            File segmentDir = collectionContext.dataFilePaths().segmentFile(collectionContext.getIndexSequence(), segmentInfo.getId());
//			int revision = segmentInfo.getRevision();
//			File revisionDir = new File(segmentDir, Integer.toString(revision));

//			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
//			boolean revisionAppended = revisionInfo.getId() > 0;
            boolean revisionHasInserts = segmentInfo.getInsertCount() > 0;
            logger.debug("머징 실행! segmentInfo={}, hasInserts={}", segmentInfo, revisionHasInserts);

            // sync파일을 append해준다.
//			if(revisionAppended){
//				if(revisionHasInserts){
//					logger.debug("revision이 추가되어, mirror file 적용!");
//					File mirrorSyncFile = new File(revisionDir, IndexFileNames.mirrorSync);
//					new MirrorSynchronizer().applyMirrorSyncFile(mirrorSyncFile, revisionDir);
//				}
//			}

            CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);

            IRService irService = ServiceManager.getInstance().getService(IRService.class);
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
//			if(revisionAppended){
//				logger.debug("revision이 추가되어, 세그먼트를 업데이트합니다.{}", segmentInfo);
//				collectionHandler.updateSegmentApplyCollection(segmentInfo, segmentDir);
//			}else{
            logger.debug("segment가 추가되어, 추가 및 적용합니다.{}", segmentInfo);
            collectionHandler.addSegmentApplyCollection(segmentInfo, segmentDir);
//			}

			/*
			 * 캐시 클리어.
			 */
            getJobExecutor().offer(new CacheServiceRestartJob());
            return new JobResult(true);

        } catch (Exception e) {
            logger.error("", e);
            throw new FastcatSearchException("ERR-00525", e);
        }
    }
    @Override
    public void readFrom(DataInput input) throws IOException {
        StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(environment);
        streamableCollectionContext.readFrom(input);
        this.collectionContext = streamableCollectionContext.collectionContext();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(collectionContext);
        streamableCollectionContext.writeTo(output);
    }

}
