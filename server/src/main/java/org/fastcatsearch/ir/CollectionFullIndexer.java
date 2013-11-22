package org.fastcatsearch.ir;

import java.io.File;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.ShardConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.index.ShardFilter;
import org.fastcatsearch.ir.index.ShardFullIndexer;
import org.fastcatsearch.ir.index.ShardIndexMapper;
import org.fastcatsearch.ir.index.ShardIndexer;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.CollectionContextUtil;

/**
 * 컬렉션의 전체색인을 수행하는 indexer.
 * */
public class CollectionFullIndexer extends AbstractCollectionIndexer {
	
	public CollectionFullIndexer(CollectionContext collectionContext) throws IRException {
		this.collectionContext = collectionContext;

		Schema workingSchema = collectionContext.workSchema();
		if (workingSchema == null) {
			workingSchema = collectionContext.schema();
		}
		File filePath = collectionContext.indexFilePaths().file();
		dataSourceReader = createDataSourceReader(filePath, workingSchema);
		
		ShardContext baseShardContext = collectionContext.getShardContext(ShardConfig.BASE_SHARD_ID);
		ShardIndexer baseShardIndexer = new ShardFullIndexer(workingSchema, baseShardContext);
		shardIndexMapper = new ShardIndexMapper(baseShardIndexer);
		
		for (ShardContext shardContext : collectionContext.getShardContextList()) {
			String shardId = shardContext.shardId();
			if(shardId.equalsIgnoreCase(ShardConfig.BASE_SHARD_ID)){
				//base shard는 mapper에 추가하지 않는다.
				continue;
			}
			String filter = shardContext.shardConfig().getFilter();
			logger.debug("#shard filter {} : {}", shardId, filter);
			ShardFilter shardFilter = new ShardFilter(workingSchema.fieldSequenceMap(), filter);
			ShardIndexer shardIndexer = new ShardFullIndexer(workingSchema, shardContext);
			if (shardIndexer != null) {
				shardIndexMapper.register(shardFilter, shardIndexer);
			}

		}

		startTime = System.currentTimeMillis();
	}

	@Override
	protected DataSourceReader createDataSourceReader(File filePath, Schema schema) throws IRException{
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		return DataSourceReaderFactory.createFullIndexingSourceReader(filePath, schema, dataSourceConfig);
	}

	public void close() throws IRException, SettingException {
		IndexStatus indexStatus = finalizeIndexing();
		collectionContext.indexStatus().setFullIndexStatus(indexStatus);
		CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
	}

}

