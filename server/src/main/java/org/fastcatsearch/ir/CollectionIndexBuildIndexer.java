package org.fastcatsearch.ir;

import java.io.File;

import org.fastcatsearch.datasource.reader.AbstractDataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.StoredDocumentSourceReader;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.index.SelectedIndexList;
import org.fastcatsearch.ir.settings.SchemaSetting;

public class CollectionIndexBuildIndexer extends AbstractCollectionIndexer {

	public CollectionIndexBuildIndexer(CollectionContext collectionContext, AnalyzerPoolManager analyzerPoolManager, SelectedIndexList selectedIndexList) throws IRException {
		super(collectionContext, analyzerPoolManager, selectedIndexList);
		init(collectionContext.schema());
	}

	@Override
	protected DataSourceReader createDataSourceReader(File filePath, SchemaSetting schemaSetting) throws IRException {
		return new StoredDocumentSourceReader(filePath, schemaSetting);
	}

	@Override
	protected void prepare() throws IRException {
		workingSegmentInfo = new SegmentInfo();
		
		// data 디렉토리를 변경한다.
		int newDataSequence = collectionContext.nextDataSequence();

		// 디렉토리 초기화.
		File indexDataDir = collectionContext.collectionFilePaths().dataPaths().indexDirFile(newDataSequence);
		if(!indexDataDir.exists()){
			throw new IRException("Index directory not found. >> " + indexDataDir);
		}
		collectionContext.clearDataInfoAndStatus();
	}

	@Override
	protected boolean done(RevisionInfo revisionInfo, IndexStatus indexStatus) throws IRException {
		int insertCount = revisionInfo.getInsertCount();

		if (insertCount > 0 && !stopRequested) {
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
			if(!stopRequested){
				logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
			}else{
				logger.info("[{}] Indexing Canceled due to Stop Requested!", collectionContext.collectionId());
			}
			return false;
		}
	}

}
