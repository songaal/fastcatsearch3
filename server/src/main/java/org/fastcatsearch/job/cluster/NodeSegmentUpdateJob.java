package org.fastcatsearch.job.cluster;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.MirrorSynchronizer;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.transport.vo.StreamableShardContext;
import org.fastcatsearch.util.CollectionContextUtil;

public class NodeSegmentUpdateJob extends StreamableJob {
	private static final long serialVersionUID = 7222232821891387399L;

	private ShardContext shardContext;

	public NodeSegmentUpdateJob() {
	}

	public NodeSegmentUpdateJob(ShardContext shardContext) {
		this.shardContext = shardContext;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			
			String collectionId = shardContext.collectionId();
			String shardId = shardContext.shardId();
			SegmentInfo segmentInfo = shardContext.dataInfo().getLastSegmentInfo();
			
			File segmentDir = shardContext.filePaths().segmentFile(shardContext.getIndexSequence(), segmentInfo.getId());
			int revision = segmentInfo.getRevision();
			File revisionDir = new File(segmentDir, Integer.toString(revision));

			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			boolean revisionAppended = revisionInfo.getId() > 0;
			boolean revisionHasInserts = revisionInfo.getInsertCount() > 0;
			logger.debug("증분업데이트 실행! revision={}, append={}, hasInserts={}", revision, revisionAppended, revisionHasInserts);
			
			// sync파일을 append해준다.
			if(revisionAppended){
				if(revisionHasInserts){
					logger.debug("revision이 추가되어, mirror file 적용!");
					File mirrorSyncFile = new File(revisionDir, IndexFileNames.mirrorSync);
					new MirrorSynchronizer().applyMirrorSyncFile(mirrorSyncFile, revisionDir);
				}
			}
			
			CollectionContextUtil.saveShardAfterIndexing(shardContext);
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			ShardHandler shardHandler = irService.collectionHandler(collectionId).getShardHandler(shardId);
			if(revisionAppended){
				logger.debug("revision이 추가되어, 세그먼트를 업데이트합니다.{}", segmentInfo);
				shardHandler.updateSegmentApplyShard(segmentInfo, segmentDir);
			}else{
				logger.debug("segment가 추가되어, 추가 및 적용합니다.{}", segmentInfo);
				shardHandler.addSegmentApplyShard(segmentInfo, segmentDir);
			}
			
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
		StreamableShardContext streamableShardContext = new StreamableShardContext(environment);
		streamableShardContext.readFrom(input);
		this.shardContext = streamableShardContext.shardContext();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		StreamableShardContext streamableShardContext = new StreamableShardContext(shardContext);
		streamableShardContext.writeTo(output);
	}

}
