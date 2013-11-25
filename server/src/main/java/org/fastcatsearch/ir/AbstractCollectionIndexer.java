package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCollectionIndexer {
	protected static final Logger logger = LoggerFactory.getLogger(CollectionFullIndexer.class);
	protected CollectionContext collectionContext;
	protected DataSourceReader dataSourceReader;
	protected long startTime;
	protected IndexingTaskState indexingTaskState;
	
	protected DeleteIdSet deleteIdSet; //삭제문서리스트. 외부에서 source reader를 통해 셋팅된다.
	
	protected IndexWriteInfoList indexWriteInfoList;
	
	protected SegmentWriter segmentWriter;
	protected SegmentInfo workingSegmentInfo;
	protected int count;
	protected long lapTime;
	
	public AbstractCollectionIndexer(CollectionContext collectionContext) {
		this.collectionContext = collectionContext;
	}
	
	protected abstract DataSourceReader createDataSourceReader(File filePath, Schema schema) throws IRException;
	protected abstract void prepare() throws IRException;
	protected abstract void done(RevisionInfo revisionInfo, IndexStatus indexStatus) throws IRException;
	
	public void init(Schema schema) throws IRException {
		
		prepare();
		
		FilePaths indexFilePaths = collectionContext.collectionFilePaths();
		int dataSequence = collectionContext.getIndexSequence();

		IndexConfig indexConfig = collectionContext.indexConfig();
		
		logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
		String segmentId = workingSegmentInfo.getId();
		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();

		File segmentDir = indexFilePaths.segmentFile(dataSequence, segmentId);
		logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
		
		segmentWriter = new SegmentWriter(schema, segmentDir, revisionInfo, indexConfig);
		
		indexWriteInfoList = new IndexWriteInfoList();
		
		File filePath = collectionContext.collectionFilePaths().file();
		dataSourceReader = createDataSourceReader(filePath, schema);
		startTime = System.currentTimeMillis();
		
		startTime = System.currentTimeMillis();
	}

	public void addDocument(Document document) throws IRException, IOException{
		segmentWriter.addDocument(document);
		count++;
		if (count % 10000 == 0) {
			logger.info(
					"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
					new Object[] { count, System.currentTimeMillis() - lapTime,
							Formatter.getFormatTime(System.currentTimeMillis() - startTime),
							Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });
			lapTime = System.currentTimeMillis();
		}
		if(indexingTaskState != null){
			indexingTaskState.incrementDocumentCount();
		}
	}
	
	public void close() throws IRException, SettingException{
		
		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
		if (segmentWriter != null) {
			try {
				segmentWriter.close();
				segmentWriter.getIndexWriteInfo(indexWriteInfoList);
			} catch (IOException e) {
				throw new IRException(e);
			}
		}

		dataSourceReader.close();
		
		logger.debug("##Indexer close {}", revisionInfo);
		deleteIdSet = dataSourceReader.getDeleteList();
		int deleteCount = deleteIdSet.size();
		
		long endTime = System.currentTimeMillis();
		
		IndexStatus indexStatus = new IndexStatus(revisionInfo.getDocumentCount(), revisionInfo.getInsertCount(), revisionInfo.getUpdateCount(), deleteCount,
				Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));
		done(revisionInfo, indexStatus);
		
		CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
	}
	
	public IndexWriteInfoList indexWriteInfoList() {
		return indexWriteInfoList;
	}
	
	public void doIndexing() throws IRException, IOException {
		
		while (dataSourceReader.hasNext()) {
			Document document = dataSourceReader.nextDocument();
			logger.debug("doc >> {}", document);
			addDocument(document);
		}

	}

//	protected IndexStatus finalizeIndexing() throws IRException, SettingException {
//		RevisionInfo info = shardIndexMapper.close();
//
//		dataSourceReader.close();
//		
//		logger.debug("##Indexer close {}", info);
//
//		deleteIdSet = dataSourceReader.getDeleteList();
//		int deleteCount = deleteIdSet.size();
//
//		long endTime = System.currentTimeMillis();
//		
//		return new IndexStatus(info.getDocumentCount(), info.getInsertCount(), info.getUpdateCount(), deleteCount,
//				Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));
//	}
	
	public DeleteIdSet deleteIdSet() {
		return deleteIdSet;
	}

	// 변경파일정보를 받아서 타 노드 전송에 사용하도록 한다.
//	public Map<String, IndexWriteInfoList> getIndexWriteInfoListMap() {
//		return shardIndexMapper.getIndexWriteInfoListMap();
//	}
	
	public void setState(IndexingTaskState indexingTaskState) {
		this.indexingTaskState = indexingTaskState;
	}
}
