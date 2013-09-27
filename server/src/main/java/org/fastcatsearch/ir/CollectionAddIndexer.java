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
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.index.ShardAddIndexer;
import org.fastcatsearch.ir.index.ShardFilter;
import org.fastcatsearch.ir.index.ShardIndexMapper;
import org.fastcatsearch.ir.index.ShardIndexer;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.CollectionContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 컬렉션의 증분색인을 수행하는 indexer.
 * */
public class CollectionAddIndexer {
	
	protected static final Logger logger = LoggerFactory.getLogger(CollectionAddIndexer.class);
			
	private ShardIndexMapper shardIndexMapper;
	private CollectionContext collectionContext;
	private DataSourceReader dataSourceReader;
	private DeleteIdSet deleteIdSet;
	private long startTime;
	
	public CollectionAddIndexer(CollectionHandler collectionHandler) throws IRException {
		collectionContext = collectionHandler.collectionContext();
		//증분색인시는 현재 스키마를 그대로 사용한다.
		Schema schema = collectionContext.schema();
		
		dataSourceReader = createDataSourceReader();
		DeleteIdSet deleteIdSet = dataSourceReader.getDeleteList();
		shardIndexMapper = new ShardIndexMapper();
		
		for(ShardContext shardContext : collectionContext.getShardContextList()){
			String shardId = shardContext.shardId();
			ShardHandler shardHandler = collectionHandler.getShardHandler(shardId);
			logger.debug("shard {} >> {}", shardId, shardHandler);
			
			String filter = shardContext.shardConfig().getFilter();
			ShardFilter shardFilter = new ShardFilter(schema.fieldSequenceMap(), filter);
			ShardIndexer shardIndexer = new ShardAddIndexer(schema, shardHandler);
			
			if(shardIndexer != null){
				shardIndexer.setDeleteIdSet(deleteIdSet); //deleteIdSet 공유.
				shardIndexMapper.register(shardFilter, shardIndexer);
			}
			
		}
		
	}
	
	protected DataSourceReader createDataSourceReader() throws IRException{
		File filePath = collectionContext.indexFilePaths().file();
		Schema schema = collectionContext.schema();
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		String lastIndexTime = collectionContext.getLastIndexTime();
		return DataSourceReaderFactory.createAddIndexingSourceReader(filePath, schema, dataSourceConfig, lastIndexTime);
	}
	
	public void doIndexing() throws IRException, IOException {
		
		
		while(dataSourceReader.hasNext()){
			Document document = dataSourceReader.nextDocument();
			logger.debug("doc >> {}", document);
			addDocument(document);
		}
		
	}
	
	public void addDocument(Document document) throws IRException, IOException{
		shardIndexMapper.addDocument(document);
	}
	
	public void close() throws IRException, SettingException {
		dataSourceReader.close();
		deleteIdSet = dataSourceReader.getDeleteList();
		
		RevisionInfo info = shardIndexMapper.close();
		int deleteCount = deleteIdSet.size();
		
		long endTime = System.currentTimeMillis();
		
		IndexStatus indexStatus = new IndexStatus(info.getDocumentCount(), info.getInsertCount(), info.getUpdateCount(), deleteCount,
				Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));
		collectionContext.indexStatus().setAddIndexStatus(indexStatus);
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
