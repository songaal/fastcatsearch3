package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.ShardContext;

public class ShardFullIndexer extends ShardIndexer {

	public ShardFullIndexer(ShardContext shardContext) throws IRException {
		super(shardContext);
	}

	@Override
	protected void prepare() throws IRException {
		// data 디렉토리를 변경한다.
		int newDataSequence = shardContext.nextDataSequence();

		// 디렉토리 초기화.
		File indexDataDir = shardContext.indexFilePaths().indexDirFile(newDataSequence);
		try {
			FileUtils.deleteDirectory(indexDataDir);
		} catch (IOException e) {
			throw new IRException(e);
		}

		shardContext.clearDataInfoAndStatus();
		indexDataDir.mkdirs();

	}

	@Override
	protected void done() throws IRException {
		int insertCount = segmentInfo.getRevisionInfo().getInsertCount();

		if (insertCount > 0) {
			shardContext.addSegmentInfo(segmentInfo);
		} else {
			logger.info("[{}] Indexing Canceled due to no documents.", shardContext.shardId());
		}

	}

}
