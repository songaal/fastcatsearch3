package org.fastcatsearch.ir;

import java.io.File;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.ShardAddIndexer;
import org.fastcatsearch.ir.index.ShardFilter;
import org.fastcatsearch.ir.index.ShardIndexMapper;
import org.fastcatsearch.ir.index.ShardIndexer;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.CollectionContextUtil;

/**
 * 컬렉션의 증분색인을 수행하는 indexer.
 * */
public class CollectionAddIndexer extends AbstractCollectionIndexer {
	
	public CollectionAddIndexer(CollectionHandler collectionHandler) throws IRException {
		collectionContext = collectionHandler.collectionContext();
		//증분색인시는 현재 스키마를 그대로 사용한다.
		Schema schema = collectionContext.schema();
		File filePath = collectionContext.indexFilePaths().file();
		dataSourceReader = createDataSourceReader(filePath, schema);
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
	
	@Override
	protected DataSourceReader createDataSourceReader(File filePath, Schema schema) throws IRException{
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		String lastIndexTime = collectionContext.getLastIndexTime();
		return DataSourceReaderFactory.createAddIndexingSourceReader(filePath, schema, dataSourceConfig, lastIndexTime);
	}
	
	public void close() throws IRException, SettingException {
		IndexStatus indexStatus = finalizeIndexing();
		logger.debug("addindexer close indexStatus > {}", indexStatus);
		collectionContext.indexStatus().setAddIndexStatus(indexStatus);
		CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
	}
	
}

