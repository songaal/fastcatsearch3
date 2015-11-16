package org.fastcatsearch.job.cluster;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
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

public class NodeSegmentUpdateJob extends Job implements Streamable {
	private static final long serialVersionUID = 7222232821891387399L;

	private CollectionContext collectionContext;

	public NodeSegmentUpdateJob() {
	}

	public NodeSegmentUpdateJob(CollectionContext collectionContext) {
		this.collectionContext = collectionContext;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			
			String collectionId = collectionContext.collectionId();
			SegmentInfo segmentInfo = collectionContext.dataInfo().getLastSegmentInfo();
			
			File segmentDir = collectionContext.dataFilePaths().segmentFile(collectionContext.getIndexSequence(), segmentInfo.getId());
//			int revision = segmentInfo.getRevision();
//			File revisionDir = new File(segmentDir, Integer.toString(revision));

//			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
//			boolean revisionAppended = revisionInfo.getId() > 0;
			boolean revisionHasInserts = segmentInfo.getInsertCount() > 0;
			logger.debug("증분업데이트 실행! segmentInfo={}, hasInserts={}", segmentInfo, revisionHasInserts);
			
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
