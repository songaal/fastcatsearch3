package org.fastcatsearch.ir;

import java.io.File;

import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.CollectionFullIndexer;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.IndexFilePaths;

public class CollectionFullIndexerApp {

	// "/Users/swsong/TEST_HOME/fastcatsearch2_shard/node1/collections/"
	public static void main(String[] args) {
		String home = args[0];
		String collectionId = args[1];
		CollectionFullIndexerApp app = new CollectionFullIndexerApp();
		app.doIndexing(home, collectionId);
	}

	private void doIndexing(String home, String collectionId) {
		try {
			IndexFilePaths paths = new IndexFilePaths(new File(home), collectionId);
			Collection collection = new Collection("sample", true);
			CollectionContext collectionContext = CollectionContextUtil.load(collection, paths);
			System.out.println(collectionContext.schema().getFieldSetting("id"));
			for (ShardContext shardContext : collectionContext.getShardContextList()) {
				System.out.println(shardContext.shardConfig().getFilter());
			}

			CollectionFullIndexer indexer = new CollectionFullIndexer(collectionContext);

			Document document = null;

			indexer.addDocument(document);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
