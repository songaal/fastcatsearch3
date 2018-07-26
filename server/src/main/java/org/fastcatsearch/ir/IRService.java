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

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.cluster.NodeLoadBalancable;
import org.fastcatsearch.common.QueryCacheModule;
import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.analysis.AnalyzerFactoryManager;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.*;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.IndexingScheduleConfig.IndexingSchedule;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentDelayedClose;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.job.PriorityScheduledJob;
import org.fastcatsearch.job.ScheduledJobEntry;
import org.fastcatsearch.job.SegmentDelayCloseScheduleJob;
import org.fastcatsearch.job.indexing.MasterCollectionAddIndexingJob;
import org.fastcatsearch.job.indexing.MasterCollectionFullIndexingJob;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.CollectionLoadErrorNotification;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SearchPageSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ScheduledExecutorService;

public class IRService extends AbstractService {

    private static final String SEGMENT_DELAY_CLOSE_SCHED_KEY = "SEGMENT_DELAY_CLOSE_SCHED";
    private Map<String, CollectionHandler> collectionHandlerMap;
    private Map<String, DynamicIndexModule> dynamicIndexModuleMap;
	private QueryCacheModule<String, Result> searchCache;
	private QueryCacheModule<String, GroupResults> groupingCache;
	private QueryCacheModule<String, GroupsData> groupingDataCache;
	private QueryCacheModule<String, Result> documentCache;
	private CollectionsConfig collectionsConfig;
	private JDBCSourceConfig jdbcSourceConfig;
	private JDBCSupportConfig jdbcSupportConfig;
	
	private SearchPageSettings searchPageSettings;
	private File collectionsRoot;

	private RealtimeQueryCountModule realtimeQueryStatisticsModule;

	private AnalyzerFactoryManager analyzerFactoryManager;
	
	private Set<String> dataNodeCollectionIdSet; //이 노드가 데이터노드인 컬렉션세트. 쿼리 count집계시 사용된다.

    //검색중인 세그먼트를 바로 닫으면 문제가 생기므로, 사용이 끝날때까지 기다렸다 close할수 있도록 저장하는 Q.
    private DelayQueue<SegmentDelayedClose> segmentDelayCloseQueue;

	public IRService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		realtimeQueryStatisticsModule = new RealtimeQueryCountModule(environment, settings);
	}

	public void setAnalyzerFactoryManager(AnalyzerProvider analyzerProvider){
		this.analyzerFactoryManager = analyzerProvider.getAnalyzerFactoryManager();
	}
	
	
	protected boolean doStart() throws FastcatSearchException {

		try{
			realtimeQueryStatisticsModule.load();
		}catch(Throwable t){
			ClusterAlertService.getInstance().alert(t);
		}
		collectionHandlerMap = new ConcurrentHashMap<String, CollectionHandler>();
        dynamicIndexModuleMap = new HashMap<String, DynamicIndexModule>();
		// collections 셋팅을 읽어온다.
		collectionsRoot = environment.filePaths().getCollectionsRoot().file();

		try {
			collectionsConfig = JAXBConfigs.readConfig(new File(collectionsRoot, SettingFileNames.collections), CollectionsConfig.class);
		} catch (JAXBException e) {
			logger.error("[ERROR] fail to read collection config. " + e.getMessage(), e);
			ClusterAlertService.getInstance().alert(e);
		}
		
		try {
			jdbcSourceConfig = JAXBConfigs.readConfig(new File(collectionsRoot, SettingFileNames.jdbcSourceConfig), JDBCSourceConfig.class);
		} catch (JAXBException e) {
			logger.error("[ERROR] fail to read jdbc source list. " + e.getMessage(), e);
			ClusterAlertService.getInstance().alert(e);
		}
		
		if(jdbcSourceConfig == null) {
			jdbcSourceConfig = new JDBCSourceConfig();
		}
		
		try {
			jdbcSupportConfig = JAXBConfigs.readConfig(new File(collectionsRoot, SettingFileNames.jdbcSupportConfig), JDBCSupportConfig.class);
		} catch (JAXBException e) {
			logger.error("[ERROR] fail to read jdbc support. " + e.getMessage(), e);
			ClusterAlertService.getInstance().alert(e);
		}
		
		if(jdbcSupportConfig == null) {
			jdbcSupportConfig = new JDBCSupportConfig();
		}
		
		File file = environment.filePaths().configPath().file(SettingFileNames.searchPageSettings);
		if(file.exists()){
			try {
				searchPageSettings = JAXBConfigs.readConfig(file, SearchPageSettings.class);
			} catch (JAXBException e) {
				logger.error("[ERROR] fail to read search page settings. " + e.getMessage(), e);
				ClusterAlertService.getInstance().alert(e);
			}
		}else{
			searchPageSettings = new SearchPageSettings();
		}

		dataNodeCollectionIdSet = new HashSet<String>();
        segmentDelayCloseQueue = new DelayQueue<SegmentDelayedClose>();

		List<Collection> collectionList = collectionsConfig.getCollectionList();
		for (int collectionInx = 0 ; collectionInx < collectionList.size(); collectionInx++) {
			Collection collection = collectionList.get(collectionInx);
			try {
				String collectionId = collection.getId();
				loadCollectionHandler(collectionId, collection);
			} catch (Throwable e) {
				logger.error("[ERROR] " + e.getMessage(), e);
			}
		}
		try {
			//가공된 컬렉션 xml 을 저장한다.
			JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.collections), 
					collectionsConfig, CollectionsConfig.class);
		} catch (JAXBException e) {
			logger.error("", e);
			ClusterAlertService.getInstance().alert(e);
		}

		searchCache = new QueryCacheModule<String, Result>(environment, settings);
		groupingCache = new QueryCacheModule<String, GroupResults>(environment, settings);
		groupingDataCache = new QueryCacheModule<String, GroupsData>(environment, settings);
		documentCache = new QueryCacheModule<String, Result>(environment, settings);
		try {
			searchCache.load();
			groupingCache.load();
			groupingDataCache.load();
			documentCache.load();
		} catch (ModuleException e) {
			ClusterAlertService.getInstance().alert(e);
			throw new FastcatSearchException("ERR-00320");
		}

        //segment close 스케쥴시작.
        JobService.getInstance().schedule(new SegmentDelayCloseScheduleJob(SEGMENT_DELAY_CLOSE_SCHED_KEY, segmentDelayCloseQueue));
		return true;
	}
	
	public CollectionHandler loadCollectionHandler(String collectionId) throws IRException, SettingException {
		return loadCollectionHandler(collectionId, null);
	}
	
	public CollectionHandler loadCollectionHandler(String collectionId, Collection collection) throws IRException, SettingException {
		Throwable t = null;
		try {
			realtimeQueryStatisticsModule.registerQueryCount(collectionId);
			
			CollectionContext collectionContext = null;
			CollectionHandler collectionHandler = null;
			logger.info("Load Collection [{}]", collectionId);
			if(collection == null){
				for (Collection col : collectionsConfig.getCollectionList()) {
					if(col.getId().equalsIgnoreCase(collectionId)){
						collection = col;
						break;
					}
				}
			}
			try {
				collectionContext = loadCollectionContext(collection);
			} catch (SettingException e) {
				logger.error("컬렉션context 로드실패 " + collectionId);
				throw e;
			}
			if (collectionContext == null) {
				return null;
			} else {
				collectionHandler = new CollectionHandler(collectionContext, analyzerFactoryManager);
				collectionHandler.setQueryCounter(realtimeQueryStatisticsModule.getQueryCounter(collectionId));
                collectionHandler.setSegmentDelayedCloseQueue(segmentDelayCloseQueue);
				if(collectionContext.collectionConfig().getDataNodeList() != null 
					&& collectionContext.collectionConfig().getDataNodeList().contains(environment.myNodeId())){
					dataNodeCollectionIdSet.add(collectionId);
				}
			}
	
			collectionHandler.load();
			
			/*
			 * 이전 컬렉션 handler가 있다면 닫아준다. 
			 */
			CollectionHandler previousCollectionHandler = collectionHandlerMap.put(collectionId, collectionHandler);
			if(previousCollectionHandler != null){
				try {
					previousCollectionHandler.close();
				} catch (IOException e) {
					throw new IRException(e);
				}
			}


			/*
			* DynamicIndexModule 을 로딩한다.
			* */
            DynamicIndexModule dynamicIndexModule = new DynamicIndexModule(environment, settings, collectionId);
			dynamicIndexModule.load();

			DynamicIndexModule prevDynamicIndexModule = dynamicIndexModuleMap.put(collectionId, dynamicIndexModule);
			if(prevDynamicIndexModule != null) {
				prevDynamicIndexModule.unload();
			}

			return collectionHandler;
			
		} catch(IRException e) {
			t = e;
			throw e;
		} catch(SettingException e) {
			t = e;
			throw e;
		} finally {
			if(t != null) {
				ClusterAlertService.getInstance().alert(t);
				NotificationService notificationService = ServiceManager.getInstance().getService(NotificationService.class);
				notificationService.sendNotification(new CollectionLoadErrorNotification(collection.getId(), t));
			}
		}
	}
	
	public JDBCSourceConfig getJDBCSourceConfig() {
		return jdbcSourceConfig;
	}
	
	public JDBCSourceInfo getJDBCSourceInfo(String jdbcId) {
		List<JDBCSourceInfo> jdbcList = jdbcSourceConfig.getJdbcSourceInfoList();
		for (JDBCSourceInfo jdbcInfo : jdbcList) {
			logger.trace("jdbc-id:{}", jdbcInfo.getId());
			if(jdbcId.equals(jdbcInfo.getId())) {
				return jdbcInfo;
			}
		}
		return null;
	}
	
	public JDBCSupportConfig getJDBCSupportConfig() {
		return jdbcSupportConfig;
	}
	
	public void updateJDBCSourceConfig(JDBCSourceConfig jdbcSourceConfig) throws JAXBException {
		this.jdbcSourceConfig = jdbcSourceConfig;
		//가공된 컬렉션 xml 을 저장한다.
		JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.jdbcSourceConfig), 
				jdbcSourceConfig, JDBCSourceConfig.class);
	}
	
	public CollectionHandler collectionHandler(String collectionId) {
		if(collectionHandlerMap !=null && collectionHandlerMap.containsKey(collectionId)) {
			return collectionHandlerMap.get(collectionId);
		}
		return null;
	}

	public CollectionContext collectionContext(String collectionId) {
		CollectionHandler h = collectionHandler(collectionId);
		if (h != null) {
			return h.collectionContext();
		} else {
			return null;
		}
	}
	
	public List<Collection> getCollectionList() {
		return collectionsConfig.getCollectionList();
	}

	public CollectionHandler createCollection(String collectionId, CollectionConfig collectionConfig, boolean loadIfExists) throws IRException, SettingException {

		if (collectionsConfig.contains(collectionId)) {
			// 이미 컬렉션 존재.
			throw new SettingException("Collection id already exists. " + collectionId);
		}

		FilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(collectionId);
		File collectionDir = collectionFilePaths.file();

		try {
			if (loadIfExists && collectionDir.exists()) {

				CollectionHandler collectionHandler = loadCollectionHandler(collectionId, new Collection(collectionId));
				collectionsConfig.addCollection(collectionId);
				JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.collections), collectionsConfig, CollectionsConfig.class);
				return collectionHandler;
			} else {
				collectionDir.mkdirs();

				CollectionContext collectionContext = CollectionContextUtil.create(collectionConfig, collectionFilePaths);
				collectionsConfig.addCollection(collectionId);
				JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.collections), collectionsConfig, CollectionsConfig.class);
				CollectionHandler collectionHandler = new CollectionHandler(collectionContext, analyzerFactoryManager);
				collectionHandlerMap.put(collectionId, collectionHandler);
				realtimeQueryStatisticsModule.registerQueryCount(collectionId);
				collectionHandler.setQueryCounter(realtimeQueryStatisticsModule.getQueryCounter(collectionId));
				collectionHandler.setSegmentDelayedCloseQueue(segmentDelayCloseQueue);
				return collectionHandler;
			}

		} catch (IRException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while create/load collection", e);
			throw new SettingException(e);
		}
	}

	public CollectionContext loadCollectionContext(Collection collection) throws SettingException {
		FilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(collection.getId());
		if (!collectionFilePaths.file().exists()) {
			// 디렉토리가 존재하지 않으면.
			logger.error("[{}]컬렉션 디렉토리가 존재하지 않습니다.", collection);
			return null;
		}
		return CollectionContextUtil.load(collection, collectionFilePaths);
	}
	
	public boolean removeCollection(String collectionId) throws SettingException {

		if (!collectionsConfig.contains(collectionId)) {
			return false;
		} else {
			try {
                DynamicIndexModule dynamicIndexModule = dynamicIndexModuleMap.remove(collectionId);
                if(dynamicIndexModule != null) {
                    dynamicIndexModule.unload();
                }
				CollectionHandler collectionHandler = collectionHandlerMap.remove(collectionId);
				if(collectionHandler != null){
					collectionHandler.close();
				}
				collectionsConfig.removeCollection(collectionId);
				JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.collections), collectionsConfig, CollectionsConfig.class);
				
				FilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(collectionId);
				//FileUtils.deleteDirectory(collectionFilePaths.file());
				if(collectionFilePaths.file().exists()) {
					FileUtils.forceDelete(collectionFilePaths.file());
				}
				return true;
			} catch (Exception e) {
				logger.error("Error while remove collection", e);
				throw new SettingException(e);
			}
		}
	}

	public CollectionHandler removeCollectionHandler(String collectionId) {
		realtimeQueryStatisticsModule.removeQueryCount(collectionId);
		return collectionHandlerMap.remove(collectionId);
	}

	public CollectionHandler putCollectionHandler(String collectionId, CollectionHandler collectionHandler) {
		//write config file if not exists
		if(!collectionHandlerMap.containsKey(collectionId)) {
			collectionsConfig.addCollection(collectionId);
			try {
				JAXBConfigs.writeConfig(new File(collectionsRoot, SettingFileNames.collections), collectionsConfig, CollectionsConfig.class);
			} catch (JAXBException e) {
				logger.error("", e);
			}
		}
		return collectionHandlerMap.put(collectionId, collectionHandler);
	}

	public CollectionHandler loadCollectionHandler(CollectionContext collectionContext) throws IRException, SettingException {
		CollectionHandler collectionHandler = new CollectionHandler(collectionContext, analyzerFactoryManager);
        collectionHandler.setSegmentDelayedCloseQueue(segmentDelayCloseQueue);
        return collectionHandler.load();
	}

	protected boolean doStop() throws FastcatSearchException {
        JobService.getInstance().cancelSchedule(SEGMENT_DELAY_CLOSE_SCHED_KEY);
		realtimeQueryStatisticsModule.unload();

        Iterator<SegmentDelayedClose> segmentCloseIter = segmentDelayCloseQueue.iterator();
        while(segmentCloseIter.hasNext()) {
            SegmentDelayedClose segmentClose = segmentCloseIter.next();
            segmentClose.closeReader();
        }
		Iterator<Entry<String, CollectionHandler>> iter = collectionHandlerMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, CollectionHandler> entry = iter.next();
			try {
				CollectionHandler collectionHandler = entry.getValue();
				if (collectionHandler != null) {
					collectionHandler.close();
				}
				logger.info("Shutdown Collection [{}]", entry.getKey());
			} catch (IOException e) {
				logger.error("[ERROR] " + e.getMessage(), e);
				throw new FastcatSearchException("IRService 종료중 에러발생.", e);
			}
		}
		searchCache.unload();
		groupingCache.unload();
		groupingDataCache.unload();
		documentCache.unload();

		collectionHandlerMap.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		collectionHandlerMap = null;
		realtimeQueryStatisticsModule = null;
        segmentDelayCloseQueue = null;
		return true;
	}

	public QueryCacheModule<String, Result> searchCache() {
		return searchCache;
	}

	public QueryCacheModule<String, GroupResults> groupingCache() {
		return groupingCache;
	}

	public QueryCacheModule<String, Result> documentCache() {
		return documentCache;
	}

	public void registerLoadBanlancer(NodeLoadBalancable nodeLoadBalancable) {
		// 차후 검색시 로드밸런싱에 대비하여 먼저 collectionId로 node들을 등록해놓는다.
		for (Collection collection : getCollectionList()) {
			String collectionId = collection.getId();
			CollectionHandler collectionHandler = collectionHandlerMap.get(collectionId);
			if (collectionHandler == null) {
				continue;
			}
			List<String> dataNodeIdList = collectionHandler.collectionContext().collectionConfig().getDataNodeList();
			nodeLoadBalancable.updateLoadBalance(collectionId, dataNodeIdList);

		}
	}

	public RealtimeQueryCountModule queryCountModule() {
		return realtimeQueryStatisticsModule;
	}
	
	private SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static String IndexingSchduleKey = "INDEXING-SCHEDULE-";
	
	public boolean reloadSchedule(String collectionId) {
		CollectionContext collectionContext = collectionContext(collectionId);
		if (collectionContext == null) {
			return false;
		}
		
		String scheduleKey = IndexingSchduleKey + collectionId;
		JobService.getInstance().cancelSchedule(scheduleKey);
		
		IndexingScheduleConfig indexingScheduleConfig = collectionContext(collectionId).indexingScheduleConfig();
		IndexingSchedule fullIndexingSchedule = indexingScheduleConfig.getFullIndexingSchedule();
		IndexingSchedule addIndexingSchedule = indexingScheduleConfig.getAddIndexingSchedule();
		
		List<ScheduledJobEntry> scheduledEntryList = new ArrayList<ScheduledJobEntry>();
		
		if (fullIndexingSchedule != null) {
			MasterCollectionFullIndexingJob job = new MasterCollectionFullIndexingJob();
			job.setArgs(collectionId);

			if (fullIndexingSchedule.isActive()) {
				String startTime = fullIndexingSchedule.getStart();
				int periodInSecond = fullIndexingSchedule.getPeriodInSecond();

				try {
					logger.debug("Load full indexing schdule {} : {}: {}", collectionId, startTime, periodInSecond);
					if(periodInSecond >= 0){
						//실행이 보장되는 스케쥴 작업으로 생성.
						scheduledEntryList.add(new ScheduledJobEntry(job, simpleDateFormat.parse(startTime), periodInSecond, true));
					}
				} catch (ParseException e) {
					logger.error("[{}] Full Indexing schedule time parse error : {}", collectionId, startTime);
					return false;
				}
			}
		}
		
		if (addIndexingSchedule != null) {
			MasterCollectionAddIndexingJob job = new MasterCollectionAddIndexingJob();
			job.setArgs(collectionId);

			if (addIndexingSchedule.isActive()) {
				String startTime = addIndexingSchedule.getStart();
				int periodInSecond = addIndexingSchedule.getPeriodInSecond();

				try {
					logger.debug("Load add indexing schdule {} : {}: {}", collectionId, startTime, periodInSecond);
					if(periodInSecond >= 0){
						scheduledEntryList.add(new ScheduledJobEntry(job, simpleDateFormat.parse(startTime), periodInSecond, false));
					}
				} catch (ParseException e) {
					logger.error("[{}] Add Indexing schedule time parse error : {}", collectionId, startTime);
					return false;
				}

			}
		}
		if(scheduledEntryList.size() > 0){
			PriorityScheduledJob scheduledJob = new PriorityScheduledJob(scheduleKey, scheduledEntryList);
			JobService.getInstance().schedule(scheduledJob, true);
		}else{
			logger.info("Collection {} has no indexing schedule.", collectionId);
		}
		return true;
	}

	public void reloadAllSchedule() {
		// 색인 스케쥴등록.
		for (CollectionsConfig.Collection collection : getCollectionList()) {
			String collectionId = collection.getId();
			reloadSchedule(collectionId);
		}
	}

	public Set<String> getDataNodeCollectionIdSet(){
		return dataNodeCollectionIdSet;
	}
	// 모든 컬렉션들의 검색노드들을 모아서 리턴한다.
	public List<String> getSearchNodeList() {
		Set<String> searchNodeSet = new HashSet<String>();
		for (CollectionHandler collectionHandler : collectionHandlerMap.values()) {
			List<String> searchNodeList = collectionHandler.collectionContext().collectionConfig().getSearchNodeList();
			if (searchNodeList != null) {
				for (String searchNodeId : searchNodeList) {
					searchNodeSet.add(searchNodeId);
				}
			}
		}
		return new ArrayList<String>(searchNodeSet);
	}

	public AnalyzerPoolManager createAnalyzerPoolManager(List<AnalyzerSetting> analyzerSettingList) {
		AnalyzerPoolManager analyzerPoolManager = new AnalyzerPoolManager();
		analyzerPoolManager.register(analyzerSettingList, analyzerFactoryManager);
		return analyzerPoolManager;
	}
	
	public SearchPageSettings getSearchPageSettings(){
		return searchPageSettings;
	}
	
	public void updateSearchPageSettings(SearchPageSettings searchPageSettings){
		this.searchPageSettings = searchPageSettings;
	}

    public DynamicIndexModule getDynamicIndexModule(String collectionId) {
        return dynamicIndexModuleMap.get(collectionId);
    }
}
