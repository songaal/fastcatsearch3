package org.fastcatsearch.ir.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardIndexMapper {
	private static Logger logger = LoggerFactory.getLogger(ShardIndexMapper.class);
	
	private Map<ShardFilter, ShardIndexer> shardFilterMap;

	public ShardIndexMapper() {
		shardFilterMap = new HashMap<ShardFilter, ShardIndexer>();
	}

	public void register(ShardFilter shardFilter, ShardIndexer shardIndexer) {
		shardFilterMap.put(shardFilter, shardIndexer);
	}

	public void addDocument(Document document) throws IRException, IOException {
		for (ShardFilter shardFilter : shardFilterMap.keySet()) {
			if (shardFilter.accept(document)) {
				shardFilterMap.get(shardFilter).addDocument(document);
				//shard filter는 서로 배타적이라는 가정하에 accept가 발견되면 바로 다음 문서로 이동한다.
				break;
			}
		}
	}

	public void close() {
		for (ShardIndexer shardIndexer : shardFilterMap.values()) {
			try {
				shardIndexer.close();
			} catch (IRException e) {
				logger.error("close error", e);
			}
		}
	}

}
