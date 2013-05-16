package org.fastcatsearch.ir.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerPoolManager {
	protected static final Logger logger = LoggerFactory.getLogger(AnalyzerPoolManager.class);
	
	private Map<String, Map<String, AnalyzerPool>> poolMap;
	
	private static final int DEFAULT_CORE_POOL_SIZE = 10; //최초 생성갯수.
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 100;//늘어났을때 유지갯수.
	
	public AnalyzerPoolManager(){
		poolMap = new ConcurrentHashMap<String, Map<String, AnalyzerPool>>();
	}
	
	public Map<String, AnalyzerPool> getPoolMap(String collectionId){
		return poolMap.get(collectionId);
	}
	
	public AnalyzerPool getPool(String collectionId, String analyzerId){
		
		Map<String, AnalyzerPool> collectionPoolMap = poolMap.get(collectionId);
		if(collectionPoolMap != null){
			AnalyzerPool pool = collectionPoolMap.get(analyzerId);
			if(pool != null){
				return pool;
			}else{
				logger.error("등록되지 않은 분석기입니다. {}/{}", collectionId, analyzerId);
			}
		}else{
			logger.error("{}컬렉션내에 분석기가 0개 입니다.", collectionId);
		}
		
		return null;
	}
	public void registerAnalyzer(String collectionId, String analyzerId, AnalyzerFactory factory){
		registerAnalyzer(collectionId, analyzerId, factory, DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE);
	}
	
	public boolean contains(String collectionId, String analyzerId){
		Map<String, AnalyzerPool> collectionPoolMap = poolMap.get(collectionId);
		if(collectionPoolMap == null){
			return false;
		}
		return collectionPoolMap.containsKey(analyzerId);
	}
	
	public void registerAnalyzer(String collectionId, String analyzerId, AnalyzerFactory factory, int corePoolSize, int maximumPoolSize) {
		
		Map<String, AnalyzerPool> collectionPoolMap = poolMap.get(collectionId);
		if(collectionPoolMap == null){
			collectionPoolMap = new HashMap<String, AnalyzerPool>();
			poolMap.put(collectionId, collectionPoolMap);
		}
		//새로들어온 분석기가 대체하도록 수정.
//		if(collectionPoolMap.containsKey(analyzerId)){
//			logger.error("이미등록된 분석기입니다.{}/{}", collectionId, analyzerId);
//			return;
//		}
		factory.init();
		
		AnalyzerPool pool = new AnalyzerPool(analyzerId, factory, corePoolSize, maximumPoolSize);
		collectionPoolMap.put(analyzerId, pool);
		
		logger.info("Register AnalyzerPool collectionId={}, analyzer={}, factory={}, core={}, max={}", collectionId, analyzerId, factory.getClass().getSimpleName(), corePoolSize, maximumPoolSize);

	}
}
