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

import javax.xml.bind.JAXBException;

import org.fastcatsearch.common.QueryCacheModule;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionStatus;
import org.fastcatsearch.ir.config.CollectionsConfig;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.JAXBConfigs;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CollectionFilePaths;

public class IRService extends AbstractService {

	private Map<String, CollectionHandler> collectionHandlerMap = new HashMap<String, CollectionHandler>();

	// TODO 캐시방식을 변경하자.

	private QueryCacheModule<Result> searchCache;
	private QueryCacheModule<InternalSearchResult> shardSearchCache;
	private QueryCacheModule<GroupResults> groupingCache;
	private QueryCacheModule<GroupsData> groupingDataCache;
	private QueryCacheModule<Result> documentCache;
	private CollectionsConfig collectionsConfig;
	private File collectionsRoot;
	
	public IRService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	protected boolean doStart() throws FastcatSearchException {

		// collectionConfigMap = new HashMap<String, CollectionConfig>();
		// collections 셋팅을 읽어온다.
		collectionsRoot = environment.filePaths().getCollectionsRoot().file();

		try {
			collectionsConfig = JAXBConfigs.readConfig(new File(collectionsRoot, SettingFileNames.collections), CollectionsConfig.class);
		} catch (JAXBException e) {
			logger.error("[ERROR] 컬렉션리스트 로딩실패. " + e.getMessage(), e);
		}

		for (Collection collection : collectionsConfig.getCollectionList()) {
			try {
				String collectionId = collection.getId();
				CollectionContext collectionContext = null;
				CollectionHandler collectionHandler = null;
				
				logger.info("Load Collection [{}]", collectionId);
				try{
					collectionContext = loadCollectionContext(collectionId);
				}catch(SettingException e){
					logger.error("컬렉션 로드실패", e);
					continue;
				}
				if (collectionContext == null) {
					if (collection.isActive()) {
						// 초기화한다.
						collectionHandler = createCollection(collectionId);
					} else {
						continue;
					}
				} else {
					collectionHandler = new CollectionHandler(collectionContext);
				}

				collectionHandlerMap.put(collectionId, collectionHandler);

				// active하지 않은 컬렉션은 map에 설정만 넣어두고 로드하지 않는다.
				if (collection.isActive()) {
					collectionHandler.load();
				}

			} catch (IRException e) {
				logger.error("[ERROR] " + e.getMessage(), e);
			} catch (SettingException e) {
				logger.error("[ERROR] " + e.getMessage(), e);
			} catch (Exception e) {
				logger.error("[ERROR] " + e.getMessage(), e);
			}
		}

		searchCache = new QueryCacheModule<Result>(environment, settings);
		shardSearchCache = new QueryCacheModule<InternalSearchResult>(environment, settings);
		groupingCache = new QueryCacheModule<GroupResults>(environment, settings);
		groupingDataCache = new QueryCacheModule<GroupsData>(environment, settings);
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

	public CollectionHandler collectionHandler(String collectionId) {
		return collectionHandlerMap.get(collectionId);
	}

	public CollectionContext collectionContext(String collectionId) {
		return collectionHandler(collectionId).collectionContext();
	}

	public List<Collection> getCollectionList() {
		return collectionsConfig.getCollectionList();
	}

	public CollectionHandler createCollection(String collectionId) throws IRException, SettingException {
		if(collectionsConfig.contains(collectionId)){
			//이미 컬렉션 존재.
			logger.error("이미 해당컬렉션이 존재함.");
			return null;
		}
		
		try{
			CollectionFilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(collectionId);
			collectionFilePaths.file().mkdirs();
			CollectionContext collectionContext = new CollectionContext(collectionId, collectionFilePaths);
			File file = environment.filePaths().configPath().file(SettingFileNames.defaultCollectionConfig);
			CollectionConfig collectionConfig = JAXBConfigs.readConfig(file, CollectionConfig.class);
			Schema schema = new Schema(new SchemaSetting());
			collectionContext.init(schema, null, collectionConfig, new DataSourceConfig(), new CollectionStatus(), new DataInfo());
			CollectionContextUtil.write(collectionContext);
			
			collectionsConfig.addCollection(collectionId, false);
			JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.collections), collectionsConfig, CollectionsConfig.class);
			CollectionHandler collectionHandler = new CollectionHandler(collectionContext);
			collectionHandlerMap.put(collectionId, collectionHandler);
			return collectionHandler;
		}catch(JAXBException e){
			throw new SettingException(e);
		}
	}

	
	// TODO 전체색인후 로드할때는 work schema를 읽어야한다.
	// schema와 sequence가 동시에 바뀔수 있음.
	public CollectionContext loadCollectionContext(String collectionId) throws SettingException {
		return loadCollectionContext(collectionId, null);
	}

	public CollectionContext loadCollectionContext(String collectionId, Integer dataSequence) throws SettingException {
		CollectionFilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(collectionId);
		if (!collectionFilePaths.file().exists()) {
			// 디렉토리가 존재하지 않으면.
			logger.error("[{}]컬렉션 디렉토리가 존재하지 않습니다.", collectionId);
			return null;
		}
		return CollectionContextUtil.load(collectionFilePaths, dataSequence);
	}

	public CollectionHandler removeCollectionHandler(String collectionId) {
		return collectionHandlerMap.remove(collectionId);
	}

	public CollectionHandler putCollectionHandler(String collectionId, CollectionHandler collectionHandler) {
		return collectionHandlerMap.put(collectionId, collectionHandler);
	}

	// 전체색인시작할때.
	public CollectionHandler initCollectionHandler(String collectionId, Integer newDataSequence) throws IRException, SettingException {
		CollectionContext collectionContext = loadCollectionContext(collectionId, newDataSequence);

		return new CollectionHandler(collectionContext);
	}

	public CollectionHandler loadCollectionHandler(CollectionContext collectionContext) throws IRException, SettingException {
		return new CollectionHandler(collectionContext).load();
	}

	public CollectionHandler loadCollectionHandler(String collectionId) throws IRException, SettingException {
		return loadCollectionHandler(collectionId, null);
	}
	public CollectionHandler loadCollectionHandler(String collectionId, Integer newDataSequence) throws IRException, SettingException {
		CollectionContext collectionContext = loadCollectionContext(collectionId, newDataSequence);

		return new CollectionHandler(collectionContext).load();
	}

	protected boolean doStop() throws FastcatSearchException {
		Iterator<Entry<String, CollectionHandler>> iter = collectionHandlerMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, CollectionHandler> entry = iter.next();
			try {
				entry.getValue().close();
				logger.info("Shutdown Collection [{}]", entry.getKey());
			} catch (IOException e) {
				logger.error("[ERROR] " + e.getMessage(), e);
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

	public QueryCacheModule<Result> searchCache() {
		return searchCache;
	}

	public QueryCacheModule<InternalSearchResult> shardSearchCache() {
		return shardSearchCache;
	}

	public QueryCacheModule<GroupResults> groupingCache() {
		return groupingCache;
	}

	public QueryCacheModule<GroupsData> groupingDataCache() {
		return groupingDataCache;
	}

	public QueryCacheModule<Result> documentCache() {
		return documentCache;
	}
}
