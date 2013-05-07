package org.fastcatsearch.ir.analysis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerPoolManager {
	protected static final Logger logger = LoggerFactory.getLogger(AnalyzerPoolManager.class);
	
	private Map<String, AnalyzerPool> poolMap = new HashMap<String, AnalyzerPool>();

	private static final int DEFAULT_CORE_POOL_SIZE = 10; //최초 생성갯수.
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 100;//늘어났을때 유지갯수.
	
	public AnalyzerPool getPool(String collectionId, String analyzerId){
		//동일 분석기에 대해서만 동기화를 건다. 
		String key = getKey(collectionId, analyzerId).intern();
		synchronized(key){
			AnalyzerPool pool = poolMap.get(key);
			if(pool != null){
				return pool;
			}else{
				logger.error("등록되지 않은 분석기입니다. {}", key);
			}
		}
		return null;
	}
	private String getKey(String collectionId, String analyzerId){
		return collectionId+"/"+analyzerId;
	}
	public void registerAnalyzer(String collectionId, String analyzerId, AnalyzerFactory factory){
		registerAnalyzer(collectionId, analyzerId, factory, DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE);
	}
	
	public boolean contains(String collectionId, String analyzerId){
		String key = getKey(collectionId, analyzerId);
		return poolMap.containsKey(key);
	}
	public void registerAnalyzer(String collectionId, String analyzerId, AnalyzerFactory factory, int corePoolSize, int maximumPoolSize) {
		String key = getKey(collectionId, analyzerId);
		if(poolMap.containsKey(key)){
			logger.error("이미등록된 분석기입니다.{}", key);
			return;
		}
		factory.init();
		AnalyzerPool pool = new AnalyzerPool(key, factory, corePoolSize, maximumPoolSize);
		poolMap.put(key, pool);
		
		logger.info("Register AnalyzerPool analyzer={}, factory={}, core={}, max={}", key, factory.getClass().getSimpleName(), corePoolSize, maximumPoolSize);

	}
}
