package org.fastcatsearch.ir.index;

import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.document.Document;

public class CollectionFullIndexer {
	
	private ShardIndexMapper shardIndexMapper;
	
	public CollectionFullIndexer(CollectionContext collectionContext) throws IRException {
		
		for(ShardContext shardContext : collectionContext.getShardContextList()){
			String filter = shardContext.shardConfig().getFilter();
			ShardFilter shardFilter = new ShardFilter(shardContext.schema().fieldSequenceMap(), filter);
			ShardIndexer shardIndexer = new ShardFullIndexer(shardContext);
			shardIndexMapper.register(shardFilter, shardIndexer);
			
		}
		
	}
	
	public void addDocument(Document document) throws IRException, IOException{
		
		shardIndexMapper.addDocument(document);
		
		
	}
	
	public void close() throws IRException{
		shardIndexMapper.close();
	}
}
