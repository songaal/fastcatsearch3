package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReaderFactory;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.job.indexing.IndexingStopException;

import java.io.File;
import java.io.IOException;

/**
 * 컬렉션의 전체색인을 수행하는 indexer.
 * */
public class CollectionFullIndexer extends AbstractCollectionIndexer {
	
	public CollectionFullIndexer(CollectionContext collectionContext, AnalyzerPoolManager analyzerPoolManager) throws IRException, IOException {
		super(collectionContext, analyzerPoolManager);

		init(collectionContext.schema());
	}

	@Override
	protected DataSourceReader createDataSourceReader(File filePath, SchemaSetting schemaSetting) throws IRException, IOException {
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		
		return DefaultDataSourceReaderFactory.createFullIndexingSourceReader(super.collectionContext.collectionId(),filePath, schemaSetting, dataSourceConfig);
	}

	@Override
	protected void prepare() throws IRException {
		workingSegmentInfo = new SegmentInfo();
		
		// data 디렉토리를 변경한다.
		int newDataSequence = collectionContext.nextDataSequence();

		// 디렉토리 초기화.
		File indexDataDir = collectionContext.collectionFilePaths().dataPaths().indexDirFile(newDataSequence);
		try {
			//FileUtils.deleteDirectory(indexDataDir);
			if(indexDataDir.exists()) {
				FileUtils.forceDelete(indexDataDir);
			}
		} catch (IOException e) {
			throw new IRException(e);
		}

		collectionContext.clearDataInfoAndStatus();
		indexDataDir.mkdirs();
	}

	@Override
	protected boolean done(SegmentInfo segmentInfo, IndexStatus indexStatus) throws IRException, IndexingStopException {
		int insertCount = segmentInfo.getDocumentCount();

        long endTime = System.currentTimeMillis();
        segmentInfo.setCreateTime(endTime);
		if(!stopRequested) {
			if (insertCount > 0) {
				//update index#/info.xml file
				//addindexing의 updateCollection대신 호출.
				collectionContext.addSegmentInfo(segmentInfo);
				//update status.xml file
				collectionContext.updateCollectionStatus(IndexingType.FULL, segmentInfo, startTime, endTime);
				collectionContext.indexStatus().setFullIndexStatus(indexStatus);
				return true;
			} else {
                indexingLogger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
				throw new IndexingStopException(collectionContext.collectionId()+" Indexing Canceled due to no documents.");
			}
		} else {
            indexingLogger.info("[{}] Indexing Canceled due to Stop Requested!", collectionContext.collectionId());
			throw new IndexingStopException(collectionContext.collectionId()+" Indexing Canceled due to Stop Requested");
		}
	}
}

