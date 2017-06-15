package org.fastcatsearch.ir;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.StoredDocumentSourceReader;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.index.IndexWritable;
import org.fastcatsearch.ir.index.SegmentIndexWriter;
import org.fastcatsearch.ir.index.SelectedIndexList;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.job.indexing.IndexingStopException;

import java.io.File;
import java.io.IOException;

public class CollectionIndexBuildIndexer extends AbstractCollectionIndexer {

	private File indexDataDir;
	
	public CollectionIndexBuildIndexer(CollectionContext collectionContext, AnalyzerPoolManager analyzerPoolManager, SelectedIndexList selectedIndexList) throws IRException, IOException {
		super(collectionContext, analyzerPoolManager, selectedIndexList);
		init(collectionContext.schema());
	}
	
	@Override
	protected IndexWritable createIndexWriter(Schema schema, File segmentDir, SegmentInfo segmentInfo, IndexConfig indexConfig) throws IRException {
		//문서를 제외한 인덱스만 생성.
		return new SegmentIndexWriter(schema, segmentDir, segmentInfo, indexConfig, analyzerPoolManager, selectedIndexList);
	}
	
	@Override
	protected DataSourceReader createDataSourceReader(File /*ignore*/filePath, SchemaSetting schemaSetting) throws IRException, IOException {
		StoredDocumentSourceReader reader = new StoredDocumentSourceReader(indexDataDir, schemaSetting);
		reader.init();
		return reader;
	}

	@Override
	protected void prepare() throws IRException {
		workingSegmentInfo = new SegmentInfo();
		// data 디렉토리를 변경한다.
		int newDataSequence = collectionContext.nextDataSequence();

		// 디렉토리 초기화.
		indexDataDir = collectionContext.collectionFilePaths().dataPaths().indexDirFile(newDataSequence);
		if(!indexDataDir.exists()){
			throw new IRException("Index directory not found. >> " + indexDataDir);
		}
		collectionContext.clearDataInfoAndStatus();
	}

	@Override
	protected boolean done(SegmentInfo segmentInfo, IndexStatus indexStatus) throws IRException, IndexingStopException {
		int insertCount = segmentInfo.getDocumentCount();

		if (insertCount > 0 && !stopRequested) {
			collectionContext.addSegmentInfo(workingSegmentInfo);
			//update status.xml file
			collectionContext.updateCollectionStatus(IndexingType.FULL, segmentInfo, startTime, System.currentTimeMillis());
			collectionContext.indexStatus().setFullIndexStatus(indexStatus);
			return false;
		}else{
			if(!stopRequested){
				logger.info("[{}] Index Build Canceled due to no documents.", collectionContext.collectionId());
				throw new IndexingStopException(collectionContext.collectionId()+" Indexing Canceled due to no documents.");
			}else{
				logger.info("[{}] Index Build Canceled due to Stop Requested!", collectionContext.collectionId());
				throw new IndexingStopException(collectionContext.collectionId()+" Indexing Canceled due to Stop Requested");
			}
		}
	}

}
