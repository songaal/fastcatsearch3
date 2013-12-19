package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.ir.search.SearchStatistics;
import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.util.AsynchronousCounter;
import org.fastcatsearch.util.AsynchronousFileLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1. 검색수치는 메모리에 이전 1분 통계 한개를 유지하며, 1분마다 master 서버에서 가져간다.
 * 
 * 2. 검색키워드기록은 buffered file writer.알아서 스위치. 2. 검색키워드는 서버마다 하루 단위로 파일로 가지고 있는다.
 * 파일은 [컬렉션명]/statistics/log/2013/12/08.log 와 같이 월단위 디렉토리로 유지한다. mater에서 자정에 요청시
 * 하루단위의 파일을 master에서 가져간다. 주,월,년 별 통계는 master에서 해당날짜에 일단위 DB통계를 이용하여 계산하도록 한다.
 * (해당날짜에 서버다운시 차후 계산하도록.. 계산여부상태를 DB에 기록하여 다음 통계시 확인.바로 이전 상태여부만 확인하면 됨.). DB는
 * 컬렉션별, 날짜타입별로 table을 따로 생성. 예를 들어 주별통계는 stat_[collectionId]_week 과 같이 된다. 3.
 * 실시간 인기검색어를 위해 5분단위로 파일에 쌓아두며, .success.1 .success.0 .fail.1 .fail.0 으로 번갈아가며
 * 유지한다. 5분마다 master에서 가져간다.
 * */
public class CategoryStatistics {
	private static final Logger logger = LoggerFactory.getLogger(CategoryStatistics.class);

	private static final int KEYWORD_LIST_DEFAULT_SIZE = 100;
	private static final int PERIOD_FOR_REALTIME_POPULAR_KEYWORD = 5;//5분.
	
	private Category category;
	
	private Timer timer;
	private List<String> keywordList;
	private AsynchronousCounter searchCounter;
	private AsynchronousFileLogger realTimeLogger;
	private AsynchronousFileLogger oneDayLogger;
	private File categoryHomeFile;
	private static final String realTimeLogFileName = ".keyword.rt.log"; //실시간 인기검색어에 사용되는 5분주기 로그.
	private static final String oneDayLogFileName = ".keyword.day.log"; //인기검색어 일,주,월 통계에 사용되는 하루주기 로그.
	
	public CategoryStatistics(Category category, File home) {
		this.category = category;
		String categoryId = category.getId();
		
		categoryHomeFile = new File(home, categoryId);
		categoryHomeFile.mkdir();
		
		
		oneDayLogger = new AsynchronousFileLogger(new File(categoryHomeFile, oneDayLogFileName));
		
		searchCounter = new AsynchronousCounter();

		keywordList = new ArrayList<String>(KEYWORD_LIST_DEFAULT_SIZE);

		timer = new Timer();
		
		/*
		 * 1. register 1 minute task.
		 * */
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MINUTE, 1); // 다음 1분후.
		timer.scheduleAtFixedRate(new Statistics1MinuteScheduler(), cal.getTime(), 60 * 1000L);
		logger.info("Statistics Category [{}] provides 1 minute keyword. start={}", categoryId, cal.getTime());
		/*
		 * 2. register 5 minute task for RealTime popular keyword 
		 * */
		if(category.isUseRealTimePopularKeyword()){
			
			realTimeLogger = new AsynchronousFileLogger(new File(categoryHomeFile, realTimeLogFileName));
			//
			while (cal.get(Calendar.MINUTE) % PERIOD_FOR_REALTIME_POPULAR_KEYWORD != 0) {
				cal.add(Calendar.MINUTE, 1); // longSchedulePeriod분단위로 맞춘다.
			}
			cal.set(Calendar.SECOND, 1);
			timer.scheduleAtFixedRate(new StatisticsRealTimeScheduler(), cal.getTime(), PERIOD_FOR_REALTIME_POPULAR_KEYWORD * 60 * 1000L);
			logger.info("Statistics Category [{}] provides realtime keyword. start={}", categoryId, cal.getTime());
		}
		
		/*
		 * 3. register 1 day task. 
		 * */
		//다음날 0시 1분 5초.
		cal.add(Calendar.DATE, 1); 
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 1); 
		cal.set(Calendar.SECOND, 5);
		timer.scheduleAtFixedRate(new StatisticsOneDayScheduler(), cal.getTime(), 24 * 60 * 60 * 1000L);
		logger.info("Statistics Category [{}] provides one day keyword. start={}", categoryId, cal.getTime());
	}

	public Category category(){
		return category;
	}
	public String categoryId(){
		return category.getId();
	}
	
	public void close() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void addStatistics(Map<String, String> statisticsData) {

		// 1. 검색수치 1증가.
		searchCounter.incrementCount();

		// 2. searchKeyword 추가.
		String searchKeyword = statisticsData.get(SearchStatistics.KEYWORD);
		if (searchKeyword != null) {
			keywordList.add(getKeywordLogString(statisticsData));
		}

	}

	private String getKeywordLogString(Map<String, String> statisticsData){
		String keyword = statisticsData.get(SearchStatistics.KEYWORD);
		String prevKeyword = statisticsData.get(SearchStatistics.PREV_KEYWORD);
		return (keyword != null ? keyword : "") + "\t" + (prevKeyword != null ? prevKeyword : ""); 
	}
	
	public int getLastCount() {
		return searchCounter.getLastCount();
	}

	// 이전 주기의 키워드 리스트 파일반환.
	public File getRealTimeKeywordFile() {
		if(realTimeLogger != null){
			return realTimeLogger.getStoredFile();
		}
		return null;
	}
	public File getOneDayKeywordFile() {
		return oneDayLogger.getStoredFile();
	}

	/*
	 * 1분마다 돌면서 키워드리스트를 파일로 내린다.
	 */
	private class Statistics1MinuteScheduler extends TimerTask {

		@Override
		public void run() {

			// counter
			searchCounter.resetCount();

			List<String> oldList = null;
			// 이전 통계 데이터 삭제.
			if (keywordList.size() > 0) {

				// 1. switch slot.
				oldList = keywordList;
				keywordList = new ArrayList<String>(KEYWORD_LIST_DEFAULT_SIZE);

			}else{
				if(oldList == null){
					oldList = new ArrayList<String>(0);
				}
				oldList.clear();
			}
			// 2. 파일로 기록. 두군데에 기록한다.
			try {
				
				if(realTimeLogger != null){
					realTimeLogger.logData(oldList);
				}
				oneDayLogger.logData(oldList);
			} catch (IOException e) {
				logger.error("", e);
			}

		}

	}

	/*
	 * longSchedulePeriod분마다 돌면서 쌓인 키워드리스트 파일을 바꿔준다. master에서 가져간다.
	 */
	private class StatisticsRealTimeScheduler extends TimerTask {
		@Override
		public void run() {
			try {
				realTimeLogger.storeAndReset();
			} catch (IOException e) {
				logger.error("", e);
			}

		}
	}
	
	private class StatisticsOneDayScheduler extends TimerTask {
		@Override
		public void run() {
			try {
				oneDayLogger.storeAndReset();
			} catch (IOException e) {
				logger.error("", e);
			}

		}
	}

}
