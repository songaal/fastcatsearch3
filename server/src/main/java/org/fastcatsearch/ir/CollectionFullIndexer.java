package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.index.ShardFilter;
import org.fastcatsearch.ir.index.ShardFullIndexer;
import org.fastcatsearch.ir.index.ShardIndexMapper;
import org.fastcatsearch.ir.index.ShardIndexer;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.CollectionContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 컬렉션의 전체색인을 수행하는 indexer.
 * */
public class CollectionFullIndexer {

	protected static final Logger logger = LoggerFactory.getLogger(CollectionFullIndexer.class);

	private ShardIndexMapper shardIndexMapper;
	private CollectionContext collectionContext;
	private DataSourceReader dataSourceReader;
	private DeleteIdSet deleteIdSet;
	private long startTime;

	public CollectionFullIndexer(CollectionContext collectionContext) throws IRException {
		this.collectionContext = collectionContext;

		Schema workingSchema = collectionContext.workSchema();
		if (workingSchema == null) {
			workingSchema = collectionContext.schema();
		}

		shardIndexMapper = new ShardIndexMapper();
		for (ShardContext shardContext : collectionContext.getShardContextList()) {
			String filter = shardContext.shardConfig().getFilter();
			logger.debug("#shard filter {} : {}", shardContext.shardId(), filter);
			ShardFilter shardFilter = new ShardFilter(workingSchema.fieldSequenceMap(), filter);
			ShardIndexer shardIndexer = new ShardFullIndexer(workingSchema, shardContext);
			if (shardIndexer != null) {
				shardIndexMapper.register(shardFilter, shardIndexer);
			}

		}

		startTime = System.currentTimeMillis();
	}

	public void doIndexing() throws IRException, IOException {
		File filePath = collectionContext.indexFilePaths().file();
		Schema schema = collectionContext.schema();
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		dataSourceReader = DataSourceReaderFactory.createFullIndexingSourceReader(filePath, schema, dataSourceConfig);

		while (dataSourceReader.hasNext()) {
			Document document = dataSourceReader.nextDocument();
//			logger.debug("doc >> {}", document);
			addDocument(document);
		}

	}

	public void addDocument(Document document) throws IRException, IOException {
		shardIndexMapper.addDocument(document);
	}

	public void close() throws IRException, SettingException {
		RevisionInfo info = shardIndexMapper.close();

		dataSourceReader.close();
		
		logger.debug("##Indexer close {}", info);

		deleteIdSet = dataSourceReader.getDeleteList();
		int deleteCount = deleteIdSet.size();

		long endTime = System.currentTimeMillis();
		
		IndexStatus indexStatus = new IndexStatus(info.getDocumentCount(), info.getInsertCount(), info.getUpdateCount(), deleteCount,
				Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));
		collectionContext.indexStatus().setFullIndexStatus(indexStatus);
		CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
	}

	public DeleteIdSet deleteIdSet() {
		return deleteIdSet;
	}

	// 변경파일정보를 받아서 타 노드 전송에 사용하도록 한다.
	public Map<String, IndexWriteInfoList> getIndexWriteInfoListMap() {
		return shardIndexMapper.getIndexWriteInfoListMap();
	}
}
