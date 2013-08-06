package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CollectionFilePaths;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionIndexer {
	protected static final Logger logger = LoggerFactory.getLogger(CollectionIndexer.class);

	private CollectionContext collectionContext;

	private DeleteIdSet deleteIdSet;
	
	public CollectionIndexer(CollectionContext collectionContext) {
		this.collectionContext = collectionContext;
	}

	public DeleteIdSet deleteIdSet(){
		return deleteIdSet;
	}
	public SegmentInfo fullIndexing() throws IOException, IRException, FastcatSearchException {
		// data 디렉토리를 변경한다.
		int newDataSequence = collectionContext.nextDataSequence();
		
		//디렉토리 초기화.
		File collectionDataDir = collectionContext.collectionFilePaths().dataFile(newDataSequence);
		FileUtils.deleteDirectory(collectionDataDir);
		
		collectionContext.clearDataInfoAndStatus();
		
		Schema schema = collectionContext.workSchema();
		if (schema == null) {
			schema = collectionContext.schema();
		}

		if (schema.getFieldSize() == 0) {
			logger.error("[{}] Full Indexing Canceled. Schema field is empty. time = {}", collectionContext.collectionId());
			throw new FastcatSearchException("[" + collectionContext.collectionId() + "] Full Indexing Canceled. Schema field is empty.");
		}
		
		SegmentInfo segmentInfo = new SegmentInfo();
		
		int revision = segmentInfo.getRevision();
		RevisionInfo revisionInfo = doIndexing(segmentInfo, revision, schema, true);
		
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
		long st = System.currentTimeMillis();

//		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
//		int dataSequence = collectionContext.getDataSequence();
		// 증분색인이면 기존스키마그대로 사용.
		Schema schema = collectionContext.schema();
		
		//logger.debug("workingHandler={}, dataSequence={}", collectionHandler, dataSequence);

		SegmentInfo workingSegmentInfo = null;

		SegmentReader lastSegmentReader = collectionHandler.getLastSegmentReader();

		if (lastSegmentReader != null) {
			SegmentInfo segmentInfo = lastSegmentReader.segmentInfo();
			int docCount = segmentInfo.getRevisionInfo().getDocumentCount();
			int segmentDocumentLimit = dataPlanConfig.getSegmentDocumentLimit();
			if (docCount >= segmentDocumentLimit) {
				// segment가 생성되는 증분색인.
				workingSegmentInfo = segmentInfo.getNextSegmentInfo();
			} else {
				// 기존 segment에 append되는 증분색인.
				workingSegmentInfo = segmentInfo.copy();
			}
		} else {
			// 로딩된 세그먼트가 없음.
			// 이전 색인정보가 없다. 즉 전체색인이 수행되지 않은 컬렉션.
			// segment가 생성되는 증분색인.
			workingSegmentInfo = new SegmentInfo();
		}

//		String segmentId = workingSegmentInfo.getId();
//		File segmentDir = collectionContext.collectionFilePaths().segmentFile(dataSequence, segmentId);
		
		int revision = workingSegmentInfo.getNextRevision();
		RevisionInfo revisionInfo = doIndexing(workingSegmentInfo, revision, schema, false);
		
		int insertCount = revisionInfo.getInsertCount();
		int deleteCount = revisionInfo.getDeleteCount();
		
		if (insertCount > 0) {
			workingSegmentInfo.updateRevision(revisionInfo);
			
			//collectinoHandler를 바꾼다.
			//FIXME 밖에서 최종적으로 update하는게 낫다.
			//여기서는 work색인만수행, 호출한곳에서 collection handler를 변경.
			//collectionHandler.updateCollection(collectionContext, workingSegmentInfo, segmentDir, deleteIdSet);
		} else {
			if (deleteCount == 0) {
				logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
				return null;
			} else {
				// count가 0이고 삭제문서만 존재할 경우 리비전은 증가하지 않은 상태.
				// FIXME
				// collectionHandler.updateSegment(sourceReader.getDeleteList());
				logger.debug("추가문서없이 삭제문서만 존재합니다.!!");
				// TODO 처리필요.
			}
		}

		return workingSegmentInfo;

	}

	public RevisionInfo doIndexing(SegmentInfo segmentInfo, int revision, Schema schema, boolean isFullIndexing) throws IRException, FastcatSearchException {

		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		int dataSequence = collectionContext.getDataSequence();
		String lastIndexTime = collectionContext.getLastIndexTime();
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		DataSourceReader sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.file(), schema, dataSourceConfig,
				lastIndexTime, isFullIndexing);

		if (sourceReader == null) {
			EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
			throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = " + dataSourceConfig);
		}

		IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
		int count = 0;
		logger.debug("WorkingSegmentInfo = {}", segmentInfo);
		RevisionInfo revisionInfo = null;
		String segmentId = segmentInfo.getId();

		File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentId);
		logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());

		SegmentWriter segmentWriter = null;
		try {
			segmentWriter = new SegmentWriter(schema, segmentDir, revision, indexConfig);
			long startTime = System.currentTimeMillis();
			long lapTime = startTime;
			while (sourceReader.hasNext()) {

				// t = System.currentTimeMillis();
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

	public SegmentInfo addIndexingBAK(CollectionHandler collectionHandler) throws IOException, IRException, FastcatSearchException {
		long st = System.currentTimeMillis();

		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
		// 증분색인이면 기존스키마그대로 사용.
		int dataSequence = collectionContext.nextDataSequence();
		Schema schema = collectionContext.schema();
		logger.debug("workingHandler={}, dataSequence={}", collectionHandler, dataSequence);

		SegmentInfo workingSegmentInfo = null;

		SegmentReader lastSegmentReader = collectionHandler.getLastSegmentReader();

		if (lastSegmentReader != null) {
			SegmentInfo segmentInfo = lastSegmentReader.segmentInfo();
			int docCount = segmentInfo.getRevisionInfo().getDocumentCount();
			int segmentDocumentLimit = dataPlanConfig.getSegmentDocumentLimit();
			if (docCount >= segmentDocumentLimit) {
				// segment가 생성되는 증분색인.
				workingSegmentInfo = segmentInfo.getNextSegmentInfo();
			} else {
				// 기존 segment에 append되는 증분색인.
				workingSegmentInfo = segmentInfo.copy();
			}
		} else {
			// 로딩된 세그먼트가 없음.
			// 이전 색인정보가 없다. 즉 전체색인이 수행되지 않은 컬렉션.
			// segment가 생성되는 증분색인.
			workingSegmentInfo = new SegmentInfo();
		}

		int revision = workingSegmentInfo.getNextRevision();

		RevisionInfo revisionInfo = doIndexing(workingSegmentInfo, revision, schema, false);

		// String lastIndexTime = collectionContext.getLastIndexTime();
		// DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		// DataSourceReader sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.file(), schema,
		// dataSourceConfig,
		// lastIndexTime, false);
		//
		// if (sourceReader == null) {
		// EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
		// throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = " + dataSourceConfig);
		// }

		// IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
		// int count = 0;
		// logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
		// File segmentDir = null;
		// RevisionInfo revisionInfo = null;
		// int revision = workingSegmentInfo.getNextRevision();
		// String segmentNumber = workingSegmentInfo.getId();

//		segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentNumber);
//		logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
//
//		SegmentWriter segmentWriter = null;
//		try {
//			segmentWriter = new SegmentWriter(schema, segmentDir, revision, indexConfig);
//			long startTime = System.currentTimeMillis();
//			long lapTime = startTime;
//			while (sourceReader.hasNext()) {
//
//				// t = System.currentTimeMillis();
//				Document doc = sourceReader.nextDocument();
//				segmentWriter.addDocument(doc);
//				count++;
//				if (count % 10000 == 0) {
//					logger.info(
//							"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
//							new Object[] { count, System.currentTimeMillis() - lapTime,
//									Formatter.getFormatTime(System.currentTimeMillis() - startTime),
//									Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });
//					lapTime = System.currentTimeMillis();
//				}
//			}
//		} catch (IRException e) {
//			logger.error("SegmentWriter Index Exception! " + e.getMessage(), e);
//			throw e;
//		} finally {
//			if (segmentWriter != null) {
//				revisionInfo = segmentWriter.close();
//				logger.debug("segmentWriter close revisionInfo={}", revisionInfo);
//			}
//			sourceReader.close();
//		}
//
//		workingSegmentInfo.updateRevision(revisionInfo);

		// schema도 apply해두어야 세그먼트 로딩시 수정된 schema로 로딩을 할수 있다.
		// 여기서는 증분색인이므로 무시.
		// 전체색인시는 collectionhandler자체를 재로딩 해야함.

		// count가 0일 경우, revision디렉토리는 삭제되었고 segmentInfo파일도 업데이트 되지 않은 상태이다.
		// count가 0일 경우, revision디렉토리는 삭제되었고 segmentInfo파일도 업데이트 되지 않은 상태이다.
		
		
		int documentCount = revisionInfo.getDocumentCount();
		int insertCount = revisionInfo.getInsertCount();
		int updateCount = revisionInfo.getUpdateCount();
		int deleteCount = revisionInfo.getDeleteCount();
		
		if (insertCount > 0) {
			workingSegmentInfo.updateRevision(revisionInfo);
			
//			collectionHandler.updateCollection(collectionContext, workingSegmentInfo, segmentDir, deleteIdSet);
		} else {
			if (deleteCount == 0) {
				logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
			} else {
				// count가 0이고 삭제문서만 존재할 경우 리비전은 증가하지 않은 상태.
				// FIXME
				// collectionHandler.updateSegment(sourceReader.getDeleteList());
				logger.debug("추가문서없이 삭제문서만 존재합니다.!!");
				// TODO 처리필요.
			}
		}

		logger.info("== SegmentStatus ==");
		collectionHandler.printSegmentStatus();
		logger.info("===================");


		collectionContext.updateCollectionStatus(IndexingType.ADD_INDEXING, revisionInfo, st, System.currentTimeMillis());

		CollectionContextUtil.saveAfterIndexing(collectionContext);

		return workingSegmentInfo;

	}
}
