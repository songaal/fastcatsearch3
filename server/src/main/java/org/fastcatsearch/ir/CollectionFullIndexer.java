package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.settings.Schema;

/**
 * 컬렉션의 전체색인을 수행하는 indexer.
 * */
public class CollectionFullIndexer extends AbstractCollectionIndexer {
	
	public CollectionFullIndexer(CollectionContext collectionContext) throws IRException {
		super(collectionContext);

//		Schema workingSchema = collectionContext.workSchema();
//		if (workingSchema == null) {
//			workingSchema = collectionContext.schema();
//		}
//		
//		init(workingSchema);
		
		init(collectionContext.schema());
	}

	@Override
	protected DataSourceReader createDataSourceReader(File filePath, Schema schema) throws IRException{
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		return DataSourceReaderFactory.createFullIndexingSourceReader(filePath, schema, dataSourceConfig);
	}

	@Override
	protected void prepare() throws IRException {
		workingSegmentInfo = new SegmentInfo();
		
		// data 디렉토리를 변경한다.
		int newDataSequence = collectionContext.nextDataSequence();

		// 디렉토리 초기화.
		File indexDataDir = collectionContext.collectionFilePaths().dataPaths().indexDirFile(newDataSequence);
		try {
			FileUtils.deleteDirectory(indexDataDir);
		} catch (IOException e) {
			throw new IRException(e);
		}

		collectionContext.clearDataInfoAndStatus();
		indexDataDir.mkdirs();
	}

	@Override
	protected boolean done(RevisionInfo revisionInfo, IndexStatus indexStatus) throws IRException {
		int insertCount = revisionInfo.getInsertCount();

		if (insertCount > 0) {
			//이미 동일한 revinfo이므로 재셋팅필요없다.
			//workingSegmentInfo.updateRevision(revisionInfo);
			
			//update index#/info.xml file
			//addindexing의 updateCollection대신 호출.
			collectionContext.addSegmentInfo(workingSegmentInfo);  
			//update status.xml file
			collectionContext.updateCollectionStatus(IndexingType.FULL, revisionInfo, startTime, System.currentTimeMillis());
			collectionContext.indexStatus().setFullIndexStatus(indexStatus);
			return true;
		}else{
			logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
			return false;
		}
		
	}
}

