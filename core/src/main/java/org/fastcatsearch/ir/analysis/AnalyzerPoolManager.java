package org.fastcatsearch.ir.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerPoolManager {
	protected static final Logger logger = LoggerFactory.getLogger(AnalyzerPoolManager.class);
	
	private Map<String, AnalyzerPool> poolMap;
	
	private static final int DEFAULT_CORE_POOL_SIZE = 10; //최초 생성갯수.
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 100;//늘어났을때 유지갯수.
	
	public AnalyzerPoolManager(){
		poolMap = new HashMap<String, AnalyzerPool>();
	}
	
	public AnalyzerPool getPool(String analyzerId){
		analyzerId = analyzerId.toUpperCase();
		if(poolMap != null){
			AnalyzerPool pool = poolMap.get(analyzerId);
			if(pool != null){
				return pool;
			}else{
				logger.error("등록되지 않은 분석기입니다. {}", analyzerId);
			}
		}else{
			logger.error("분석기가 0개 입니다.");
		}
		
		return null;
	}
	public void registerAnalyzer(String analyzerId, AnalyzerFactory factory){
		registerAnalyzer(analyzerId, factory, DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE);
	}
	
	public boolean contains(String analyzerId){
		if(poolMap == null){
			return false;
		}
		return poolMap.containsKey(analyzerId);
	}
	
	public void registerAnalyzer(String analyzerId, AnalyzerFactory factory, int corePoolSize, int maximumPoolSize) {
		
		if(poolMap == null){
			poolMap = new HashMap<String, AnalyzerPool>();
		}
		
		factory.init();
		
		AnalyzerPool pool = new AnalyzerPool(analyzerId, factory, corePoolSize, maximumPoolSize);
		poolMap.put(analyzerId, pool);
		
		logger.info("Register AnalyzerPool analyzer={}, factory={}, core={}, max={}", analyzerId, factory.getClass().getSimpleName(), corePoolSize, maximumPoolSize);

	}

	public void register(List<AnalyzerSetting> analyzerSettingList, AnalyzerFactoryManager analyzerFactoryManager) {
		//AnalyzerSettingList 로 AnalyzerPoolManager에 등록한다.
		for(AnalyzerSetting setting : analyzerSettingList){
			String analyzerFactoryId = setting.getClassName();
			if(analyzerFactoryId == null || analyzerFactoryId.trim().length() == 0) {
				logger.error("Analyzer id empty >> {}", analyzerFactoryId);
				continue;
			}
			AnalyzerFactory factory = analyzerFactoryManager.getAnalyzerFactory(analyzerFactoryId);
			if(factory != null){
				registerAnalyzer(setting.getId(), factory, setting.getCorePoolSize(), setting.getMaximumPoolSize());
			}else{
				logger.error("Cannot find analyzer factory id >> {}", analyzerFactoryId);
			}
		}
		
	}
}
