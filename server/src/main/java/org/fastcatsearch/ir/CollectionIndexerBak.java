package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionIndexerBak {
	protected static final Logger logger = LoggerFactory.getLogger(CollectionIndexerBak.class);

	private CollectionContext collectionContext;

	private DeleteIdSet deleteIdSet;
	private IndexWriteInfoList indexWriteInfoList;

	public CollectionIndexerBak(CollectionContext collectionContext) {
		this.collectionContext = collectionContext;
	}

	public DeleteIdSet deleteIdSet() {
		return deleteIdSet;
	}

	public IndexWriteInfoList indexWriteInfoList() {
		return indexWriteInfoList;
	}

	public SegmentInfo fullIndexing() throws IOException, IRException, FastcatSearchException {
		// data 디렉토리를 변경한다.
		int newDataSequence = collectionContext.nextDataSequence();

		// 디렉토리 초기화.
		File collectionDataDir = collectionContext.collectionFilePaths().dataFile(newDataSequence);
		FileUtils.deleteDirectory(collectionDataDir);

		collectionContext.clearDataInfoAndStatus();
		collectionDataDir.mkdirs();
		
		Schema schema = collectionContext.workSchema();
		if (schema == null) {
			schema = collectionContext.schema();
		}

		if (schema.getFieldSize() == 0) {
			logger.error("[{}] Full Indexing Canceled. Schema field is empty. time = {}", collectionContext.collectionId());
			throw new FastcatSearchException("[" + collectionContext.collectionId() + "] Full Indexing Canceled. Schema field is empty.");
		}

		SegmentInfo segmentInfo = new SegmentInfo();

		DataSourceReader sourceReader = getSourceReader(schema, true);
		RevisionInfo revisionInfo = doIndexing(sourceReader, segmentInfo, schema);

		int insertCount = revisionInfo.getInsertCount();

		if (insertCount > 0) {
			segmentInfo.updateRevision(revisionInfo);
			collectionContext.addSegmentInfo(segmentInfo);
		} else {
			logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
		}

		return segmentInfo;
	}

	public SegmentInfo addIndexing(CollectionHandler collectionHandler) throws IOException, IRException, FastcatSearchException {
		DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
		// 증분색인이면 기존스키마그대로 사용.
		Schema schema = collectionContext.schema();
		int dataSequence = collectionContext.getDataSequence();
		// logger.debug("workingHandler={}, dataSequence={}", collectionHandler, dataSequence);

		SegmentInfo workingSegmentInfo = null;

		SegmentReader lastSegmentReader = collectionHandler.getLastSegmentReader();

		if (lastSegmentReader != null) {
			SegmentInfo segmentInfo = lastSegmentReader.segmentInfo();
			int docCount = segmentInfo.getRevisionInfo().getDocumentCount();
			int segmentDocumentLimit = dataPlanConfig.getSegmentDocumentLimit();
			if (docCount >= segmentDocumentLimit) {
				// segment가 생성되는 증분색인.
				workingSegmentInfo = segmentInfo.getNextSegmentInfo();
				File segmentDir = collectionContext.collectionFilePaths().segmentFile(dataSequence, workingSegmentInfo.getId());
				logger.debug("#색인시 세그먼트를 생성합니다. {}", workingSegmentInfo);
				FileUtils.removeDirectoryCascade(segmentDir);
			} else {
				// 기존 segment에 append되는 증분색인.
				workingSegmentInfo = segmentInfo.copy();
				// 리비전을 증가시킨다.
				int revision = workingSegmentInfo.nextRevision();
				File segmentDir = collectionContext.collectionFilePaths().segmentFile(dataSequence, workingSegmentInfo.getId());
				File revisionDir = new File(segmentDir, Integer.toString(revision));
				FileUtils.removeDirectoryCascade(revisionDir);
				logger.debug("#색인시 리비전을 증가합니다. {}", workingSegmentInfo);
			}
		} else {
			// 로딩된 세그먼트가 없음.
			// 이전 색인정보가 없다. 즉 전체색인이 수행되지 않은 컬렉션.
			// segment가 생성되는 증분색인.
			workingSegmentInfo = new SegmentInfo();
			File segmentDir = collectionContext.collectionFilePaths().segmentFile(dataSequence, workingSegmentInfo.getId());
			logger.debug("#이전 세그먼트가 없어서 색인시 세그먼트를 생성합니다. {}", workingSegmentInfo);
			FileUtils.removeDirectoryCascade(segmentDir);
		}

		logger.debug("증분색인용 SegmentInfo={}", workingSegmentInfo);
		
		DataSourceReader sourceReader = getSourceReader(schema, false);
		RevisionInfo revisionInfo = doIndexing(sourceReader, workingSegmentInfo, schema);

		int insertCount = revisionInfo.getInsertCount();
		int deleteCount = revisionInfo.getDeleteCount();

		if (insertCount > 0) {
			workingSegmentInfo.updateRevision(revisionInfo);
			collectionContext.updateSegmentInfo(workingSegmentInfo);
		} else {
			if (deleteCount == 0) {
				logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
				return null;
			} else {
				// count가 0이고 삭제문서만 존재할 경우 리비전은 증가하지 않은 상태.
				// FIXME
				logger.debug("추가문서없이 삭제문서만 존재합니다.!!");
				// TODO 처리필요.
			}
		}

		return workingSegmentInfo;

	}

	protected DataSourceReader getSourceReader(Schema schema, boolean isFullIndexing) throws FastcatSearchException {
		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		String lastIndexTime = collectionContext.getLastIndexTime();
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		DataSourceReader sourceReader = null;
		try {
			sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.file(), schema, dataSourceConfig, lastIndexTime,
					isFullIndexing);
		} catch (IRException e) {
			throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = " + dataSourceConfig);
		}
		return sourceReader;
	}

	/*
	 * 주어진 SegmentInfo를 바탕으로 색인을 수행한다. 색인데이터는 sourceReader로 부터 받는다.
	 */
	protected RevisionInfo doIndexing(DataSourceReader sourceReader, SegmentInfo segmentInfo, Schema schema) throws IRException,
			FastcatSearchException {
		indexWriteInfoList = new IndexWriteInfoList();

		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		int dataSequence = collectionContext.getDataSequence();

		IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
		int count = 0;
		logger.debug("WorkingSegmentInfo = {}", segmentInfo);
		RevisionInfo revisionInfo = null;
		String segmentId = segmentInfo.getId();
		int revision = segmentInfo.getRevision();

		File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentId);
		logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());

		SegmentWriter segmentWriter = null;
		try {
			segmentWriter = new SegmentWriter(schema, segmentDir, revision, indexConfig);
			long startTime = System.currentTimeMillis();
			long lapTime = startTime;
			while (sourceReader.hasNext()) {

				Document doc = sourceReader.nextDocument();
				segmentWriter.addDocument(doc);
				count++;
				if (count % 10000 == 0) {
					logger.info(
							"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
							new Object[] { count, System.currentTimeMillis() - lapTime,
									Formatter.getFormatTime(System.currentTimeMillis() - startTime),
									Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });
					lapTime = System.currentTimeMillis();
				}
			}
		} catch (IRException e) {
			logger.error("SegmentWriter Index Exception! " + e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			logger.error("SegmentWriter Index Exception! " + e.getMessage(), e);
			throw new IRException(e);
		} finally {
			if (segmentWriter != null) {
				try {
					revisionInfo = segmentWriter.close();

					segmentWriter.getIndexWriteInfo(indexWriteInfoList);
					logger.debug("segmentWriter close revisionInfo={}", revisionInfo);
				} catch (IOException e) {
					throw new IRException(e);
				}
			}
			sourceReader.close();
		}

		deleteIdSet = sourceReader.getDeleteList();
		int deleteCount = deleteIdSet.size();
		revisionInfo.setDeleteCount(deleteCount);

		return revisionInfo;

	}


}
