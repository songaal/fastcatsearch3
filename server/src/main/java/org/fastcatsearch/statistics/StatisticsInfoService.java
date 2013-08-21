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

package org.fastcatsearch.statistics;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.db.dao.SearchEvent;
import org.fastcatsearch.db.dao.SearchMonitoringInfo;
import org.fastcatsearch.db.dao.SearchMonitoringInfoMinute;
import org.fastcatsearch.db.vo.IndexingResultVO;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;


/**
 * 검색쿼리요청을 분석하여 초당쿼리수, 응답시간등을 1초에 한번 통계내는 서비스.
 * IRService시작후에 시작되어야 한다.
 * @author swsong
 *
 */
public class StatisticsInfoService extends AbstractService {

	private static final String GLOBAL_COLLECTION_NAME = "__global__";
	private static long PERIOD = 1000; //1초마다 Task를 수행한다.
	private static long PERIOD_2S = 1000 * 2;
	private static long PERIOD_1M = 1000 * 60;
	private static long PERIOD_1H = 1000 * 60 * 60;
	private static long PERIOD_1D = 1000 * 60 * 60 * 24;
	private static long START_DELAY = 100;
	private Timer timer;
	private boolean isEnabled = false; //기본적으로 사용한다.
	private AsciiCharTrie collectionSeq;
	private String[] collectionNameList;
	private RealTimeCollectionStatistics[] collectionStatisticsList;
	private RealTimeCollectionStatistics globalCollectionStatistics; //컬렉션 구분없이 사용되는 통계정보.
	private RealTimeCollectionStatistics[] collectionStatisticsListPerMinute;
	private RealTimeCollectionStatistics globalCollectionStatisticsPerMinute;
	private RealTimeCollectionStatistics[] collectionStatisticsListPerHour;
	private RealTimeCollectionStatistics globalCollectionStatisticsPerHour;
	private RealTimeCollectionStatistics[] collectionStatisticsListPerDay;
	private RealTimeCollectionStatistics globalCollectionStatisticsPerDay;
	private RealTimeCollectionStatistics[] collectionStatisticsListPerMonth;
	private RealTimeCollectionStatistics globalCollectionStatisticsPerMonth;
	private IndexingInfo[] indexingInfoList;
	private Timestamp lastUpdatedIndexingTime;
	private Timestamp lastUpdatedPopularKeywordTime;
	private Timestamp lastUpdatedEventTime; //이벤트가 업데이트된 시간.
	private boolean[] isCollectionLive;
	private SearchKeywordCache keywordCache;
	private int countPerMinute; //1분에 몇번의 초별 계산이 수행되었는가? 60이 정상.
	private int countPerHour; //1시간에 몇번의 초별 계산이 수행되었는가? 60이 정상.
	private int countPerDay; //1시간에 몇번의 초별 계산이 수행되었는가? 60이 정상.
	private int countPerMonth; //1시간에 몇번의 초별 계산이 수행되었는가? 60이 정상.
	
	private static StatisticsInfoService instance;
	private	IRService irService;
	
	public static StatisticsInfoService getInstance(){
		return instance;
	}
	public StatisticsInfoService(Environment environment, Settings settings, ServiceManager serviceManager){
		super(environment, settings, serviceManager);
	}
	
	@Override
	protected boolean doStart() throws FastcatSearchException {
		
		lastUpdatedIndexingTime = new Timestamp(0);
		lastUpdatedPopularKeywordTime = new Timestamp(0);
		lastUpdatedEventTime = new Timestamp(0);
		keywordCache = new SearchKeywordCache();
		irService = ServiceManager.getInstance().getService(IRService.class);
		List<Collection> collectionList = irService.getCollectionList();
		int collectionSize = collectionList.size();
		collectionNameList = new String[collectionSize];
		for (int i = 0; i < collectionNameList.length; i++) {
			collectionNameList[i] = collectionList.get(i).getId();
		}
		collectionSeq = new AsciiCharTrie();
		collectionStatisticsList = new RealTimeCollectionStatistics[collectionSize];
		collectionStatisticsListPerMinute = new RealTimeCollectionStatistics[collectionSize];
		collectionStatisticsListPerHour = new RealTimeCollectionStatistics[collectionSize];
		collectionStatisticsListPerDay = new RealTimeCollectionStatistics[collectionSize];
		collectionStatisticsListPerMonth = new RealTimeCollectionStatistics[collectionSize];
		
		globalCollectionStatistics = new RealTimeCollectionStatistics(GLOBAL_COLLECTION_NAME);
		globalCollectionStatisticsPerMinute = new RealTimeCollectionStatistics(GLOBAL_COLLECTION_NAME);
		globalCollectionStatisticsPerHour = new RealTimeCollectionStatistics(GLOBAL_COLLECTION_NAME);
		globalCollectionStatisticsPerDay = new RealTimeCollectionStatistics(GLOBAL_COLLECTION_NAME);
		globalCollectionStatisticsPerMonth = new RealTimeCollectionStatistics(GLOBAL_COLLECTION_NAME);
		
		indexingInfoList = new IndexingInfo[collectionSize];
		
		for(int i = 0; i < collectionSize; i++){
			String collectionName = collectionNameList[i];
			collectionSeq.put(collectionName, i);
			collectionStatisticsList[i] = new RealTimeCollectionStatistics(collectionName);
			indexingInfoList[i] = new IndexingInfo(collectionName);
			
			collectionStatisticsListPerMinute[i] = new RealTimeCollectionStatistics(collectionName);
			collectionStatisticsListPerHour[i] = new RealTimeCollectionStatistics(collectionName);
			collectionStatisticsListPerDay[i] = new RealTimeCollectionStatistics(collectionName);
			collectionStatisticsListPerMonth[i] = new RealTimeCollectionStatistics(collectionName);
		}
		
		timer = new Timer(true);
		timer.schedule(new StatisticsTask(), START_DELAY, PERIOD);
		timer.schedule(new IndexingInfoTask(), START_DELAY, PERIOD_2S);
		timer.schedule(new SearchKeywordTask(), START_DELAY, PERIOD);
		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.SECOND, 0);
		startTime.add(Calendar.MINUTE, 1);
		//다음 minute의 0초에 시작한다.
		timer.schedule(new StatisticsPerMinuteTask(), startTime.getTime(), PERIOD_1M);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.add(Calendar.HOUR, 1);
		timer.schedule(new StatisticsPerHourTask(), startTime.getTime(), PERIOD_1H);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.HOUR, 0);
		startTime.add(Calendar.DATE, 1);
		timer.schedule(new StatisticsPerDayTask(), startTime.getTime(), PERIOD_1D);
		
		//PopularKeywordUpdatedCheckTask는 2초마다 수행.
		timer.schedule(new PopularKeywordUpdatedCheckTask(), START_DELAY, PERIOD_2S);
		timer.schedule(new EventUpdatedCheckTask(), START_DELAY, PERIOD_2S);
		getCollectionStatus();
		
		isEnabled = true;
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		timer.cancel();
		timer = null;
		isEnabled = false;
		return true;
	}
	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}
	
	public void setEnable(){
		isEnabled = true;
	}
	
	public void setDisable(){
		isEnabled = false;
	}
	
	public boolean isEnabled(){
		return isEnabled;
	}
	
	public RealTimeCollectionStatistics[] getCollectionStatisticsList(){
		if(!isRunning())
			return null;
		
		return collectionStatisticsList;
	}
	
	public RealTimeCollectionStatistics getGlobalCollectionStatistics(){
		if(!isRunning())
			return null;
		
		return globalCollectionStatistics;
	}
	
	public IndexingInfo[] getIndexingInfoList(){
		if(!isRunning())
			return null;
		
		synchronized(indexingInfoList){
			return indexingInfoList;
		}
	}
	
	//즉시 IndexingInfo의 업데이트 결과를 보고 싶을때 호출한다.
	public void forceRunIndexingInfoTask(){
		new Thread(new IndexingInfoTask()).start();
	}
	
	//컬렉션의 고유 번호 반환
	private int getCollectionId(String collection){
		int id = collectionSeq.get(collection);
		return id;
	}
	public String[] getCollectionNameList(){
		return collectionNameList;
	}
	//컬렉션의 서비스start여부를 리턴한다.
	//순서는 collectionNameList에 담겨있는 순서이다.
	public boolean[] getCollectionStatus(){
		boolean[] result = new boolean[collectionNameList.length];
		for(int i = 0; i < collectionNameList.length; i++){
			String collectionName = collectionNameList[i];
			result[i] = (irService.collectionHandler(collectionName) != null);
		}
		//update isCollectionLive
		isCollectionLive = result;
		return result;
	}
	
	//1. 검색전 호출, 모든 쿼리
	//모든 검색쿼리에 대한 hit수를 수집한다.
	//검색전에 호출함으로써 검색실패한 요청도 통계에 포함되도록 한다.
	//[검색활동량]
	public void addSearchHit(){
		globalCollectionStatistics.addHit();
	}
	public void addSearchHit(String collection){
		int id = getCollectionId(collection);
		if(id >= 0){
			collectionStatisticsList[id].addHit();
		}
	}
	//2. 검색후 호출, 실패 쿼리. 컬렉션구분없이
	public void addFailHit(){
		globalCollectionStatistics.addFailHit();
	}
	//2. 검색후 호출, 실패 쿼리. 컬렉션별.
	public void addFailHit(String collection){
		int id = getCollectionId(collection);
		if(id >= 0){
			collectionStatisticsList[id].addFailHit();
		}
	}
	
	//3. 검색후 호출, 모든 쿼리
	//검색종료후 모든쿼리에 대한 시간을 수집한다.
	//모든 검색쿼리에 대해서 수집함으로써 정확한 응답시간 통게를 낼수 있다.
	//[검색응답시간(평균, 최대)]
	public void addSearchTime(long searchTime){
		globalCollectionStatistics.addTime(searchTime);
	}
	public void addSearchTime(String collection, long searchTime){
		int id = getCollectionId(collection);
		if(id >= 0){
			collectionStatisticsList[id].addTime(searchTime);
		}
	}
	
	//4. 검색전 호출, 선별적 쿼리
	//검색수행전에 ud를 통해 keyword로 셋팅한 키워드에 대해서 통계를 수집한다.
	//모든 키워드를 수집하게되면, 코드값과 불필요한 키워드들이 포함될수 있기 때문에 사용자에게 제공하기에는 데이터가 깨끗하지 못하다.
	//사용자가 보고 싶어하는 검색어는 검색창에 입력한 단어 위주이기때문에, ud에 셋팅한 키워드만 수집하도록 한다.
	//[실시간 검색로그], [실시간 인기키워드]
	public void addSearchKeyword(String keyword){
		keywordCache.addKeyword(keyword);
	}
	
	public SearchKeywordCache getKeywordCache(){
		return keywordCache;
	}
	
	class StatisticsTask extends TimerTask{

		@Override
		public void run() {
			for (int i = 0; i < isCollectionLive.length; i++) {
				if(isCollectionLive[i]){
					collectionStatisticsList[i].checkHitStatictics();
					collectionStatisticsList[i].checkResponseTime();
//					collectionStatisticsList[i].print();
					collectionStatisticsListPerMinute[i].add(collectionStatisticsList[i]);
				}
			}
			globalCollectionStatistics.checkResponseTime();
			globalCollectionStatistics.checkHitStatictics();
			globalCollectionStatisticsPerMinute.add(globalCollectionStatistics);
//			globalCollectionStatistics.print();
			countPerMinute++;
		}
	}
	
	class StatisticsPerMinuteTask extends TimerTask{

		@Override
		public void run() {
			if(countPerMinute == 0)
				countPerMinute = 1;
			
			Timestamp when = new Timestamp(System.currentTimeMillis());
			logger.debug("검색 정보 1분 통계. countPerMinute = {}", countPerMinute);
			//global정보먼저 입력
			RealTimeCollectionStatistics stat = globalCollectionStatisticsPerMinute.getAverage(countPerMinute);
			stat.print();
			//DB에 입력한다.
			DBService.getInstance().db().getDAO("SearchMonitoringInfoMinute", SearchMonitoringInfoMinute.class).insert(stat.getCollectionName(), stat.getHitPerUnitTime(), stat.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat.getMeanResponseTime(), stat.getMaxResponseTime(), when);
			//컬렉션별 정보입력
			for (int i = 0; i < isCollectionLive.length; i++) {
				if(isCollectionLive[i]){
					stat = collectionStatisticsListPerMinute[i].getAverage(countPerMinute);
					stat.print();
					//DB에 입력한다.
					DBService.getInstance().db().getDAO("SearchMonitoringInfoMinute", SearchMonitoringInfoMinute.class).insert(stat.getCollectionName(), stat.getHitPerUnitTime(), stat.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat.getMeanResponseTime(), stat.getMaxResponseTime(), when);
					collectionStatisticsListPerHour[i].add(stat);
				}
			}
			
//			DBHandler.getInstance().commitMon();
			countPerMinute = 0;
			
			globalCollectionStatisticsPerHour.add(stat);
			countPerHour++;
			
		}
	}
	
	class StatisticsPerHourTask extends TimerTask{

		@Override
		public void run() {
			/*
			 * 1. 시간마다 시간별 통계내기
			 * */
			if(countPerHour == 0)
				countPerHour = 1;
			
			Timestamp when = new Timestamp(System.currentTimeMillis());
			logger.debug("검색 정보 1시간 통계. countPerHour = {}", countPerHour);
			//global정보먼저 입력
			RealTimeCollectionStatistics stat = globalCollectionStatisticsPerHour.getAverage(countPerHour);
			stat.print();
			//DB에 입력한다.
			DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).insert(stat.getCollectionName(), stat.getHitPerUnitTime(), stat.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat.getMeanResponseTime(), stat.getMaxResponseTime(), when, "h");
			
			globalCollectionStatisticsPerDay.add(stat);
			countPerDay++;
			
			//컬렉션별 정보입력
			for (int i = 0; i < isCollectionLive.length; i++) {
				if(isCollectionLive[i]){
					stat = collectionStatisticsListPerHour[i].getAverage(countPerHour);
					stat.print();
					//DB에 입력한다.
					DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).insert(stat.getCollectionName(), stat.getHitPerUnitTime(), stat.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat.getMeanResponseTime(), stat.getMaxResponseTime(), when, "h");
					collectionStatisticsListPerDay[i].add(stat);
				}
			}
			
			countPerHour = 0;
			
//			DBHandler.getInstance().commitMon();
		}
	}
	
	class StatisticsPerDayTask extends TimerTask{

		@Override
		public void run() {
			/*
			 * 1. 날마다 시간별 통계내기
			 * */
			if(countPerDay == 0)
				countPerDay = 1;
			
			Timestamp when = new Timestamp(System.currentTimeMillis());
			logger.debug("검색 정보 1일 통계. countPerDay = {}", countPerDay);
			//global정보먼저 입력
			RealTimeCollectionStatistics stat = globalCollectionStatisticsPerDay.getAverage(countPerDay);
			stat.print();
			//DB에 입력한다.
			DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).insert(stat.getCollectionName(), stat.getHitPerUnitTime(), stat.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat.getMeanResponseTime(), stat.getMaxResponseTime(), when, "d");
			
			// 매월 1일 전달 통계저장하기.
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int day = calendar.get(Calendar.DATE);
			if (day == 1) {
				RealTimeCollectionStatistics stat_m = globalCollectionStatisticsPerMonth.getAverage(countPerMonth);
				stat_m.print();
				//DB에 입력한다.
				DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).insert(stat_m.getCollectionName(), stat_m.getHitPerUnitTime(), stat_m.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat_m.getMeanResponseTime(), stat_m.getMaxResponseTime(), when, "m");
			}
			globalCollectionStatisticsPerMonth.add(stat);
			countPerMonth++;
			
			//컬렉션별 정보입력
			for (int i = 0; i < isCollectionLive.length; i++) {
				if(isCollectionLive[i]){
					stat = collectionStatisticsListPerDay[i].getAverage(countPerDay);
					stat.print();
					//DB에 입력한다.
					DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).insert(stat.getCollectionName(), stat.getHitPerUnitTime(), stat.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat.getMeanResponseTime(), stat.getMaxResponseTime(), when, "d");
					if (day == 1) {
						RealTimeCollectionStatistics stat_m = collectionStatisticsListPerMonth[i].getAverage(countPerMonth);
						stat_m.print();
						//DB에 입력한다.
						DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).insert(stat_m.getCollectionName(), stat_m.getHitPerUnitTime(), stat_m.getFailHitPerUnitTime(), stat.getAccumulatedHit(), stat.getAccumulatedFailHit(), stat_m.getMeanResponseTime(), stat_m.getMaxResponseTime(), when, "m");
					}
					collectionStatisticsListPerMonth[i].add(stat);
				}
			}
			
			countPerDay = 0;
			
			/*
			 * 2. 이전 데이터 지워주기.
			 * */
			DBService.getInstance().db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).deleteOld(1);//1달 이전은 삭제 
			
			
//			DBHandler.getInstance().commitMon();
		}
	}
	
	class IndexingInfoTask extends TimerTask{

		@Override
		public void run() {
	    	DBService dbHandler = DBService.getInstance();
	    	IndexingResult indexingResult = dbHandler.db().getDAO("IndexingResult", IndexingResult.class);
			//색인시간이 업데이트 되었는가?
	    	Timestamp updateTime = indexingResult.isUpdated(lastUpdatedIndexingTime);

			//만약 색인db가 업데이트 되었다면 IndexingInfo를 갱신한다.
			if(updateTime != null){
				logger.debug("색인 DB가 업데이트됨..UPDATE_TIME={}",updateTime);
				
			
		    	synchronized(indexingInfoList){
					for (int i = 0; i < isCollectionLive.length; i++) {
						//업데이트 여부 확인
						//업데이트 되었으면 채워넣고, 안 되었으면 그대로...
						//처음시작시에만 모두 채워넣는다.
						if(isCollectionLive[i]){
			    			IndexingResultVO fullResult = indexingResult.select(collectionNameList[i], "FULL");
			    			IndexingResultVO incResult = indexingResult.select(collectionNameList[i], "ADD");
			    			CollectionHandler collectionHandler = irService.collectionHandler(collectionNameList[i]);
			    			int docCount = 0;
			    			if(collectionHandler != null){
			    				docCount = 0;
				    			int segmentSize = collectionHandler.segmentSize();
				    			for (int j = 0; j < segmentSize; j++) {
				    				SegmentInfo seginfo = collectionHandler.segmentReader(j).segmentInfo();
				    				docCount += seginfo.getRevisionInfo().getDocumentCount();
								}
			    			}
			    			indexingInfoList[i].reset();
			    			if(fullResult != null){
			    				indexingInfoList[i].fullDoc = fullResult.docSize;
			    				indexingInfoList[i].fullInsert = fullResult.docSize - fullResult.updateSize - fullResult.deleteSize;
				    			indexingInfoList[i].fullUpdate = fullResult.updateSize;
				    			indexingInfoList[i].fullDelete = fullResult.deleteSize;
			    			}else{
			    				
			    			}
			    			
			    			if(incResult != null){
			    				indexingInfoList[i].incDoc = incResult.docSize;
			    				indexingInfoList[i].incInsert = incResult.docSize - incResult.updateSize - incResult.deleteSize;
			    				indexingInfoList[i].incUpdate = incResult.updateSize;
			    				indexingInfoList[i].incDelete = incResult.deleteSize;
			    			}
			    			indexingInfoList[i].totalDoc = docCount;
			    			indexingInfoList[i].updateTime = System.currentTimeMillis();
						}
					}//for
		    	}
		    	//시간 업데이트
		    	lastUpdatedIndexingTime = updateTime;
			}else{
//				logger.debug("색인 DB가 업데이트 되지않음..lasttime = {}", lastUpdatedIndexingTime);
			}
		}
		
	}
	//SearchKeywordCache의 슬롯을 주기적으로 변경해준다.
	class SearchKeywordTask extends TimerTask{

		@Override
		public void run() {
			keywordCache.switchSlot();
			
		}
	}
	class PopularKeywordUpdatedCheckTask extends TimerTask{

		@Override
		public void run() {
//			DBService dbHandler = DBService.getInstance();
//			//업데이트 되었는가?
//	    	Timestamp updateTime = dbHandler.db().getDAO("KeywordHit", KeywordHit.class).isPoluarKeywordUpdated(lastUpdatedPopularKeywordTime);
//
//			//만약 업데이트 되었다면 시간을 갱신한다.
//			if(updateTime != null){
//				logger.debug("인기검색어 리스트가 업데이트됨..UPDATE_TIME={}",updateTime);
//				lastUpdatedPopularKeywordTime = updateTime;
//			}else{
////				logger.debug("인기검색어 업데이트X..LAST_TIME={}",lastUpdatedPopularKeywordTime);
//			}
		}
		
	}
	class EventUpdatedCheckTask extends TimerTask{

		@Override
		public void run() {
			DBService dbHandler = DBService.getInstance();
			//업데이트 되었는가?
	    	Timestamp updateTime = dbHandler.db().getDAO("SearchEvent", SearchEvent.class).isUpdated(lastUpdatedEventTime);
			//만약 업데이트 되었다면 시간을 갱신한다.
			if(updateTime != null){
				logger.debug("이벤트 리스트가 업데이트됨..UPDATE_TIME={}", updateTime);
				lastUpdatedEventTime = updateTime;
			}else{
//				logger.debug("인기검색어 업데이트X..LAST_TIME={}",lastUpdatedEventTime);
			}
		}
		
	}

	public boolean isIndexingInfoUpdated(long lastUpdateTime) {
		if(!isRunning())
			return false;
		
		if(lastUpdatedIndexingTime.getTime() >  lastUpdateTime){
			return true;
		}
		
		return false;
	}

	public boolean isPopularKeywordUpdated(long lastUpdateTime) {
		if(!isRunning())
			return false;
		
		if(lastUpdatedPopularKeywordTime.getTime() >  lastUpdateTime){
			return true;
		}
		
		return false;
	}
	
	public boolean isEventUpdated(long lastUpdateTime) {
		if(!isRunning())
			return false;
		
		if(lastUpdatedEventTime.getTime() >  lastUpdateTime){
			return true;
		}
		
		return false;
	}

}
