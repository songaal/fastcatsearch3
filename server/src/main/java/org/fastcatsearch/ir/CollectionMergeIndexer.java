package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReaderFactory;
import org.fastcatsearch.datasource.reader.StoredDocumentSourceReader;
import org.fastcatsearch.datasource.reader.StoredSegmentSourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.job.indexing.IndexingStopException;
import org.fastcatsearch.util.CoreFileUtils;
import org.fastcatsearch.util.FilePaths;

import java.io.File;
import java.io.IOException;

/**
 * 컬렉션의 세그먼트 머징을 수행하는 indexer.
 * */
public class CollectionMergeIndexer extends AbstractCollectionIndexer {

	private CollectionHandler collectionHandler;

    private File[] segmentDirs;

	public CollectionMergeIndexer(CollectionHandler collectionHandler, File... segmentDirs) throws IRException {
		super(collectionHandler.collectionContext(), collectionHandler.analyzerPoolManager());
		this.collectionHandler = collectionHandler;
		this.segmentDirs = segmentDirs;
		//증분색인시는 현재 스키마를 그대로 사용한다.
        init(collectionContext.schema());
	}
	
	@Override
	protected DataSourceReader createDataSourceReader(File filePath, SchemaSetting schemaSetting) throws IRException{
        StoredSegmentSourceReader reader = new StoredSegmentSourceReader(segmentDirs, schemaSetting);
        reader.init();
        return reader;
	}

	/*
	 * workingSegmentInfo 객체를 준비한다.
	 * */
	@Override
	protected void prepare() throws IRException {
		FilePaths indexFilePaths = collectionContext.indexFilePaths();
		// 증분색인이면 기존스키마그대로 사용.
        String newSegmentId = collectionHandler.nextSegmentId();
        workingSegmentInfo = new SegmentInfo(newSegmentId);
        File segmentDir = indexFilePaths.file(workingSegmentInfo.getId());
        logger.debug("#색인시 세그먼트를 생성합니다. {}", workingSegmentInfo);
        try {
            CoreFileUtils.removeDirectoryCascade(segmentDir);
        } catch (IOException e) {
            throw new IRException(e);
        }
	}

	@Override
	protected boolean done(SegmentInfo segmentInfo, IndexStatus indexStatus) throws IRException, IndexingStopException {

        int insertCount = segmentInfo.getInsertCount();
        int deleteCount = segmentInfo.getDeleteCount();
        FilePaths indexFilePaths = collectionContext.indexFilePaths();
        File segmentDir = indexFilePaths.file(segmentInfo.getId());

        try {
            if (!stopRequested) {
                if (insertCount <= 0) {
                    // 세그먼트 증가시 segment디렉토리 삭제.
                    logger.debug("# 머징문서가 없으므로, segment를 삭제합니다. {}", segmentDir.getAbsolutePath());
                    FileUtils.deleteDirectory(segmentDir);
                    logger.info("delete segment dir ={}", segmentDir.getAbsolutePath());
                    segmentInfo.resetCountInfo();
                }
                if (deleteCount > 0) {
                    segmentInfo.setDeleteCount(deleteCount);
                }

                if (insertCount <= 0 && deleteCount <= 0) {
                    logger.info("[{}] Merging is canceled due to no documents.", collectionContext.collectionId());
                    throw new IndexingStopException(collectionContext.collectionId() + " Merging is canceled due to no documents.");
                }

                collectionHandler.updateCollection(collectionContext, segmentInfo, segmentDir, deleteIdSet);

                //status.xml 업데이트
                collectionContext.updateCollectionStatus(IndexingType.ADD, segmentInfo, startTime, System.currentTimeMillis());
                collectionContext.indexStatus().setAddIndexStatus(indexStatus);

            } else {
                FileUtils.deleteDirectory(segmentDir);
                logger.info("delete segment dir ={}", segmentDir.getAbsolutePath());
                logger.info("[{}] Indexing Canceled due to Stop Requested!", collectionContext.collectionId());
                throw new IndexingStopException(collectionContext.collectionId() + " Indexing Canceled due to Stop Requested");
            }
        } catch (IOException e) {
            throw new IRException(e);
        }

        return true;
	}
	
}

