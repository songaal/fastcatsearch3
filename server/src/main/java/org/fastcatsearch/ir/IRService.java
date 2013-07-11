/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.common.QueryCacheModule;
import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.JAXBConfigs;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.ShardSearchResult;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.CollectionContextUtil;


public class IRService extends AbstractService{
	
	private Map<String, CollectionHandler> collectionHandlerMap = new HashMap<String, CollectionHandler>();
	
	//TODO 캐시방식을 변경하자.
	
	private QueryCacheModule<Result> searchCache;
	private QueryCacheModule<ShardSearchResult> shardSearchCache;
	private QueryCacheModule<GroupResults> groupingCache;
	private QueryCacheModule<GroupData> groupingDataCache;
	private QueryCacheModule<Result> documentCache;
//	private Map<String, CollectionConfig> collectionConfigMap;
	private CollectionsConfig collectionsConfig;
	
	public IRService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}
	
	protected boolean doStart() throws FastcatSearchException {
		
//		collectionConfigMap = new HashMap<String, CollectionConfig>(); 
		// collections 셋팅을 읽어온다.
		File collectionsRoot = environment.filePaths().getCollectionsRoot().file();
		
		collectionsConfig = JAXBConfigs.readConfig(new File(collectionsRoot, SettingFileNames.collections), CollectionsConfig.class);
		
		for (Collection collection : collectionsConfig.getCollectionList()) {
			try {
				String collectionId = collection.getId();
				
				CollectionContext collectionContext = loadCollectionContext(collectionId); 
				
				CollectionHandler collectionHandler = new CollectionHandler(collectionContext);
				collectionHandlerMap.put(collectionId, collectionHandler);
				
				if(!collection.isActive()){
					//active하지 않은 컬렉션은 map에 설정만 넣어두고 시작하지 않는다.
					continue;
				}

//				IndexConfig indexConfig = collectionConfig.getIndexConfig();
				
//				collectionHandlerMap.put(collectionId, new CollectionHandler(collectionId, collectionDir, collectionContext[i], indexConfig));
			} catch (IRException e) {
				logger.error("[ERROR] "+e.getMessage(),e);
			} catch (SettingException e) {
				logger.error("[ERROR] "+e.getMessage(),e);
			} catch (Exception e) {
				logger.error("[ERROR] "+e.getMessage(),e);
			}
		}
		
		searchCache = new QueryCacheModule<Result>(environment, settings);
		shardSearchCache = new QueryCacheModule<ShardSearchResult>(environment, settings);
		groupingCache = new QueryCacheModule<GroupResults>(environment, settings);
		groupingDataCache = new QueryCacheModule<GroupData>(environment, settings);
		documentCache = new QueryCacheModule<Result>(environment, settings);
		try {
			searchCache.load();
			shardSearchCache.load();
			groupingCache.load();
			groupingDataCache.load();
			documentCache.load();
		} catch (ModuleException e) {
			throw new FastcatSearchException("ERR-00320");
		}
		return true;
	}
	
	public CollectionHandler collectionHandler(String collectionId){
		return collectionHandlerMap.get(collectionId);
	}
	
	public CollectionContext collectionContext(String collectionId){
		return collectionHandler(collectionId).collectionContext();
	}
	
	public List<Collection> getCollectionList(){
		return collectionsConfig.getCollectionList();
	}
	
	//TODO 전체색인후 로드할때는 work schema를 읽어야한다.
	//schema와 sequence가 동시에 바뀔수 있음.
	public CollectionContext loadCollectionContext(String collectionId){
		return loadCollectionContext(collectionId, -1);
	}
	
	public CollectionContext loadCollectionContext(String collectionId, int dataSequence){
		CollectionFilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(collectionId);
		return CollectionContextUtil.load(collectionFilePaths, dataSequence);
	}
	
	public CollectionHandler removeCollectionHandler(String collectionId){
		return collectionHandlerMap.remove(collectionId);
	}
	
	
	public CollectionHandler putCollectionHandler(String collectionId, CollectionHandler collectionHandler){
		return collectionHandlerMap.put(collectionId, collectionHandler);
	}

	//전체색인시작할때.
	public CollectionHandler initCollectionHandler(String collectionId, int newDataSequence) throws IRException, SettingException{
		CollectionContext collectionContext = loadCollectionContext(collectionId, newDataSequence); 
		
		return new CollectionHandler(collectionContext);
	}
	
	public CollectionHandler loadCollectionHandler(String collectionId, int newDataSequence) throws IRException, SettingException{
		CollectionContext collectionContext = loadCollectionContext(collectionId, newDataSequence); 
		
		return new CollectionHandler(collectionContext);
	}
	
//	public CollectionConfig getCollectionConfig(String collectionId){
//		return collectionConfigMap.get(collectionId);
//	}
	
	protected boolean doStop() throws FastcatSearchException {
		Iterator<Entry<String, CollectionHandler>> iter = collectionHandlerMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, CollectionHandler> entry = iter.next();
			try {
				entry.getValue().close();
				logger.info("Collection " + entry.getKey()+ " Shutdown!");
			} catch (IOException e) {
				logger.error("[ERROR] "+e.getMessage(),e);
				throw new FastcatSearchException("IRService 종료중 에러발생.", e);
			}
		}
		searchCache.unload();
		shardSearchCache.unload();
		groupingCache.unload();
		groupingDataCache.unload();
		documentCache.unload();
		return true;
	}	
	
	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}
	
	public QueryCacheModule<Result> searchCache(){
		return searchCache;
	}
	
	public QueryCacheModule<ShardSearchResult> shardSearchCache(){
		return shardSearchCache;
	}
	
	public QueryCacheModule<GroupResults> groupingCache(){
		return groupingCache;
	}
	
	public QueryCacheModule<GroupData> groupingDataCache(){
		return groupingDataCache;
	}
	
	public QueryCacheModule<Result> documentCache(){
		return documentCache;
	}
}
