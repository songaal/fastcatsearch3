package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionConfig.Shard;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.util.IndexFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionHandler {
	private static Logger logger = LoggerFactory.getLogger(ShardHandler.class);
	private String collectionId;
	private CollectionContext collectionContext;
	private Map<String, ShardHandler> shardHandlerMap;

	private long startedTime;
	private boolean isLoaded;
	private IndexFilePaths indexFilePaths;

	public CollectionHandler(CollectionContext collectionContext) throws IRException, SettingException {
		this.collectionContext = collectionContext;
		this.collectionId = collectionContext.collectionId();
		this.indexFilePaths = collectionContext.indexFilePaths();
	}

	public CollectionHandler load() throws IRException {
		shardHandlerMap = new HashMap<String, ShardHandler>();

		loadShards();
		startedTime = System.currentTimeMillis();
		isLoaded = true;
		logger.info("Collection[{}] Loaded! {}", collectionId, indexFilePaths.file().getAbsolutePath());
		return this;
	}

	private void loadShards() throws IRException {
		for(ShardContext shardContext : collectionContext.getShardContextList()){
			ShardHandler shardHandler = new ShardHandler(shardContext);
			shardHandler.load();
			shardHandlerMap.put(shardHandler.shardId(), shardHandler);
		}
//		List<Shard> shardList = collectionContext.collectionConfig().getShardConfigList();
//		for (Shard shard : shardList) {
//			String shardId = shard.getId();
//			ShardContext shardContext = collectionContext.getShardContext(shardId);
//
//			ShardHandler shardHandler = new ShardHandler(shardContext);
//			shardHandler.load();
//			shardHandlerMap.put(shardId, shardHandler);
//		}
	}

	public Map<String, ShardHandler> shardHandlerMap() {
		return shardHandlerMap;
	}

	public ShardHandler getShardHandler(String shardId) {
		return shardHandlerMap.get(shardId);
	}

	public void changeShardHandler(ShardContext shardContext) throws IRException {
		ShardHandler shardHandler = new ShardHandler(shardContext).load();
		ShardHandler oldShardHandler = shardHandlerMap.put(shardHandler.shardId(), shardHandler);
		try {
			if (oldShardHandler != null) {
				oldShardHandler.close();
			}
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	public long getStartedTime() {
		return startedTime;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public CollectionContext collectionContext() {
		return collectionContext;
	}

	public void close() throws IOException {
		for (Entry<String, ShardHandler> entry : shardHandlerMap.entrySet()) {
			ShardHandler shardHandler = entry.getValue();
			shardHandler.close();
		}
		
		shardHandlerMap.clear();
		
		isLoaded = false;
		

	}

	public void printSegmentStatus() {
		for(ShardHandler shardHandler : shardHandlerMap.values()){
			logger.info("--- {} ---", shardHandler.shardId());
			shardHandler.printSegmentStatus();
		}
	}
}
