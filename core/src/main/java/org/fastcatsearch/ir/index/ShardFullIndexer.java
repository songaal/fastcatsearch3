//package org.fastcatsearch.ir.index;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.apache.commons.io.FileUtils;
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.common.IndexingType;
//import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
//import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
//import org.fastcatsearch.ir.config.ShardContext;
//import org.fastcatsearch.ir.settings.Schema;
//
//public class ShardFullIndexer extends ShardIndexer {
//
//	public ShardFullIndexer(Schema schema, ShardContext shardContext) throws IRException {
//		super(shardContext);
//		init(schema);
//	}
//
//	@Override
//	protected void prepare() throws IRException {
//		workingSegmentInfo = new SegmentInfo();
//		
//		// data 디렉토리를 변경한다.
//		int newDataSequence = shardContext.nextDataSequence();
//
//		// 디렉토리 초기화.
//		File indexDataDir = shardContext.filePaths().indexDirFile(newDataSequence);
//		try {
//			FileUtils.deleteDirectory(indexDataDir);
//		} catch (IOException e) {
//			throw new IRException(e);
//		}
//
//		shardContext.clearDataInfoAndStatus();
//		indexDataDir.mkdirs();
//
//	}
//
//	@Override
//	protected void done() throws IRException {
//		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
//		int insertCount = revisionInfo.getInsertCount();
//
//		if (insertCount > 0) {
//			workingSegmentInfo.updateRevision(revisionInfo);
//			//update index#/info.xml file
//			shardContext.addSegmentInfo(workingSegmentInfo);
//			//update status.xml file
//			shardContext.updateIndexingStatus(IndexingType.FULL, revisionInfo, startTime, System.currentTimeMillis());
//		}
//		
//	}
//
//}
