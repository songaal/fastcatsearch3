package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.index.ShardIndexMapper;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCollectionIndexer {
	protected static final Logger logger = LoggerFactory.getLogger(CollectionFullIndexer.class);
	protected ShardIndexMapper shardIndexMapper;
	protected CollectionContext collectionContext;
	protected DataSourceReader dataSourceReader;
	protected DeleteIdSet deleteIdSet;
	protected long startTime;
	protected IndexingTaskState indexingTaskState;
	
	
	protected abstract DataSourceReader createDataSourceReader(File filePath, Schema schema) throws IRException;
	
	public void doIndexing() throws IRException, IOException {
		
		while (dataSourceReader.hasNext()) {
			Document document = dataSourceReader.nextDocument();
			logger.debug("doc >> {}", document);
			addDocument(document);
		}

	}

	public void addDocument(Document document) throws IRException, IOException {
		shardIndexMapper.addDocument(document);
		if(indexingTaskState != null){
			indexingTaskState.incrementDocumentCount();
		}
	}
	
	protected IndexStatus finalizeIndexing() throws IRException, SettingException {
		RevisionInfo info = shardIndexMapper.close();

		dataSourceReader.close();
		
		logger.debug("##Indexer close {}", info);

		deleteIdSet = dataSourceReader.getDeleteList();
		int deleteCount = deleteIdSet.size();

		long endTime = System.currentTimeMillis();
		
		return new IndexStatus(info.getDocumentCount(), info.getInsertCount(), info.getUpdateCount(), deleteCount,
				Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));
	}
	
	public DeleteIdSet deleteIdSet() {
		return deleteIdSet;
	}

	// 변경파일정보를 받아서 타 노드 전송에 사용하도록 한다.
	public Map<String, IndexWriteInfoList> getIndexWriteInfoListMap() {
		return shardIndexMapper.getIndexWriteInfoListMap();
	}
	
	public void setState(IndexingTaskState indexingTaskState) {
		this.indexingTaskState = indexingTaskState;
	}
}
