//package org.fastcatsearch.ir.index;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
//import org.fastcatsearch.ir.document.Document;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class ShardIndexMapper {
//	private static Logger logger = LoggerFactory.getLogger(ShardIndexMapper.class);
//	
//	private Map<ShardFilter, ShardIndexer> shardFilterMap;
//	private Map<String, IndexWriteInfoList> shardIndexWriteInfoListMap;
//	private Set<Map.Entry<ShardFilter,ShardIndexer>> entrySet;
//	ShardIndexer baseShardIndexer;
//	
//	public ShardIndexMapper(ShardIndexer baseShardIndexer) {
//		this.baseShardIndexer = baseShardIndexer;
//		shardFilterMap = new HashMap<ShardFilter, ShardIndexer>();
//		shardIndexWriteInfoListMap = new HashMap<String, IndexWriteInfoList>();
//	}
//
//	public void register(ShardFilter shardFilter, ShardIndexer shardIndexer) {
//		shardFilterMap.put(shardFilter, shardIndexer);
//		entrySet = shardFilterMap.entrySet();
//		logger.debug("register > {}", entrySet);
//	}
//
//	public void addDocument(Document document) throws IRException, IOException {
//		logger.debug("addDocument > {}", document);
//		if(entrySet != null){
//			for (Map.Entry<ShardFilter,ShardIndexer> shardFilterEntry : entrySet) {
//				if (shardFilterEntry.getKey().accept(document)) {
//					logger.debug("accept shard {} >> {}", shardFilterEntry.getValue().shardContext().shardId(), document.get(0));
//					shardFilterEntry.getValue().addDocument(document);
//					//shard filter는 서로 배타적이라는 가정하에 accept가 발견되면 바로 다음 문서로 이동한다.
//					//Note : 동일문서가 여러 shard에 추가되면, 통합검색시 동일문서가 출현하게 되므로, pk로 걸러주는 작업이 필요하다.
//					return;
//				}
//			}
//		}
//		//shard에 포함안된 문서는 base shard가 해결한다.
//		baseShardIndexer.addDocument(document);
//	}
//
//	public RevisionInfo close() {
//		//하위 shard의 데이터를 모두 더해서 리턴한다.
//		RevisionInfo totalInfo = new RevisionInfo();
//		for (ShardIndexer shardIndexer : shardFilterMap.values()) {
//			try {
//				RevisionInfo revisionInfo = shardIndexer.close();
//				totalInfo.add(revisionInfo);
//				
//				String shardId = shardIndexer.shardContext().shardId();
//				IndexWriteInfoList indexWriteInfoList = shardIndexer.indexWriteInfoList();
//				shardIndexWriteInfoListMap.put(shardId, indexWriteInfoList);
//			} catch (IRException e) {
//				logger.error("close error", e);
//			}
//		}
//		try {
//			RevisionInfo revisionInfo = baseShardIndexer.close();
//			totalInfo.add(revisionInfo);
//			String shardId = baseShardIndexer.shardContext().shardId();
//			IndexWriteInfoList indexWriteInfoList = baseShardIndexer.indexWriteInfoList();
//			shardIndexWriteInfoListMap.put(shardId, indexWriteInfoList);
//		} catch (IRException e) {
//			logger.error("close error", e);
//		}
//		return totalInfo;
//	}
//
//	//색인파일 기록 정보를 제공한다.
//	public Map<String, IndexWriteInfoList> getIndexWriteInfoListMap() {
//		return shardIndexWriteInfoListMap;
//	}
//
//}
