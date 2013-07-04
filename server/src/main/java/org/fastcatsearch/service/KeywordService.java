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

package org.fastcatsearch.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KeywordService extends AbstractService{

	private static final Logger logger = LoggerFactory.getLogger(KeywordService.class);
	
	public static final long FIVE_MINUTE_PERIOD = 5 * 60 * 1000;
	public static final long ONE_SECOND_PERIOD = 1000;
	public static final int MAX_KEYWORD_COUNT = 100;
	public static final int MAX_KEYWORD_VALUE = 999;
	//NOTE: this constant value is temporarily hard coding
	public static final double POPULAR_CONSTANT = 0.3;
	
	public static final int LIMIT_TIME_DATE = -31; //1개월분
	public static final int LIMIT_TIME_WEEK = -23; //6개월분
	public static final int LIMIT_TIME_MONTH = -24; //2년분
	public static final int LIMIT_TIME_YEAR = -10; //10년분
	
	private Timer timer;
	private int hourOfDay;
	private int sequence;
	private String logPath = environment.filePaths().makePath("logs").append(".keyword.").toString();
	private String logPathFail = environment.filePaths().makePath("logs").append(".keyword.fail.").toString();
	private PrintWriter keywordWriter;
	private PrintWriter keywordWriterFail;
	
	private static KeywordService instance;
	
	public static KeywordService getInstance(){
		return instance;
	}
	
	public KeywordService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}
	public void addKeyword(String keyword){
		//1hour 파일에 기록
		if(keywordWriter != null){
			logger.debug("addKeyword = "+keyword);
			synchronized(keywordWriter){
				keywordWriter.println(keyword);
			}
		}
	}
	public void addFailKeyword(String keyword) {
		//1hour 파일에 기록
		if(keywordWriterFail != null){
			logger.debug("addFailKeyword = "+keyword);
			synchronized(keywordWriterFail){
				keywordWriterFail.println(keyword);
			}
		}
	}
	private int switchFile(){
		
		if(keywordWriter != null)
			keywordWriter.close();
		
		int old = sequence;
		hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		sequence = (sequence + 1) % 2;
//		logger.debug("Switch keyword file "+old +" -> "+sequence+", hourOfDay="+hourOfDay);
		
		try {
			File file = new File(logPath + sequence);
			if(!file.exists()) { file.createNewFile(); }
			keywordWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e) {
			//logger.error(e.getMessage(),e);
		}
		return old;
	}
	
	private int switchFileFail(){
		
		if(keywordWriterFail != null)
			keywordWriterFail.close();
		
		int old = sequence;
		hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		sequence = (sequence + 1) % 2;
//		logger.debug("Switch fail keyword file "+old +" -> "+sequence+", hourOfDay="+hourOfDay);
		
		try {
			File file = new File(logPathFail + sequence);
			if(!file.exists()) { file.createNewFile(); }
			keywordWriterFail = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e) {
			//logger.error(e.getMessage(),e);
		}
		return old;
	}
	
	protected boolean doStart() throws FastcatSearchException {
		//현재 시각에서 가장 가까운 5로 나누어 떨어지는 이전 시각의 분 숫자를 구함. nowMin
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int nowMin = cal.get(Calendar.MINUTE);
		nowMin = nowMin - nowMin%5;
		
		//데몬 쓰레드로 시작해야 메인쓰레드가 종료할때 같이 종료됨.
		timer = new Timer(true);
		Calendar baseCalendar = Calendar.getInstance();
//		baseCalendar.set(Calendar.MINUTE, 0); //DEBUG
		//예: 현재시각이 2012-05-08 16:28:00 이면 2012-05-08 16:25:00로 셋팅해줌.
		baseCalendar.set(Calendar.MINUTE, nowMin);
		baseCalendar.set(Calendar.SECOND, 0);
		baseCalendar.set(Calendar.MILLISECOND, 0);
		//다음 정각부터 랭킹계산 태스트를 수행한다.
		//baseCalendar.add(Calendar.HOUR_OF_DAY, 1);
//		baseCalendar.add(Calendar.SECOND, 60); //DEBUG
//		baseCalendar.add(Calendar.MILLISECOND,(int)FIVE_MINUTE_PERIOD);
		
		logger.debug("BaseCalendar = "+baseCalendar.getTime());
		
		switchFile();
		switchFileFail();
//		try{
//			logger.debug("register keyword tasks!!");
//			timer.scheduleAtFixedRate(new KeywordRankingTask(), baseCalendar.getTime(), FIVE_MINUTE_PERIOD);
//			timer.scheduleAtFixedRate(new KeywordFailRankingTask(), baseCalendar.getTime(), FIVE_MINUTE_PERIOD);
//		}catch(IllegalStateException e){
//			logger.debug("error! retry register keyword tasks!!");
//			timer = new Timer(true);
//			timer.scheduleAtFixedRate(new KeywordRankingTask(), baseCalendar.getTime(), FIVE_MINUTE_PERIOD);
//			timer.scheduleAtFixedRate(new KeywordFailRankingTask(), baseCalendar.getTime(), FIVE_MINUTE_PERIOD);
//		}
//		timer.schedule(new StreamFlushTask(), new Date(), ONE_SECOND_PERIOD); //flush from now
		logger.debug("Keyword schedule done!");
		return true;
	}
	
	protected boolean doStop() throws FastcatSearchException {
		timer.cancel();
		return true;
	}
	
//	public static void calculateDate(int type, Calendar todayCalendar, Date[] dates, int[] times) {
//		Calendar c = Calendar.getInstance();
//		c.setTime(todayCalendar.getTime());
//		c.set(Calendar.HOUR,0);
//		c.set(Calendar.MINUTE,0);
//		c.set(Calendar.SECOND, 0);
//		c.set(Calendar.MILLISECOND,0);
//		
//		switch (type) {
//			case KeywordHit.STATISTICS_DATE:
//				dates[0] = c.getTime();
//				times[0] = c.get(Calendar.DATE); //오늘
//				c.add(Calendar.DATE, -1);
//				dates[1] = c.getTime();
//				times[1] = c.get(Calendar.DATE); //어제
//				c.add(Calendar.DATE, LIMIT_TIME_DATE); //일누적 만료일자
//				dates[2] = c.getTime();
//				break;
//			case KeywordHit.STATISTICS_WEEK:
//				c.add(Calendar.DATE, c.get(Calendar.DAY_OF_WEEK)*-1+2);
//				dates[0] = c.getTime();
//				times[0] = c.get(Calendar.DAY_OF_WEEK)-2; //이번주
//				c.add(Calendar.DATE, -7);
//				dates[1] = c.getTime();
//				times[1] = c.get(Calendar.DAY_OF_WEEK)-2; //전주
//				c.add(Calendar.DATE, LIMIT_TIME_WEEK * 7); //주누적 만료일자
//				dates[2] = c.getTime();
//				break;
//			case KeywordHit.STATISTICS_MONTH:
//				c.set(Calendar.DATE, 1);
//				dates[0] = c.getTime();
//				times[0] = c.get(Calendar.MONTH)+1; //이번달
//				c.add(Calendar.MONTH, -1);
//				dates[1] = c.getTime();
//				times[1] = c.get(Calendar.MONTH)+1; //전달
//				c.add(Calendar.MONTH, LIMIT_TIME_MONTH);
//				dates[2] = c.getTime();
//				break;
//			case KeywordHit.STATISTICS_YEAR:
//				c.set(Calendar.DATE, 1);
//				c.set(Calendar.MONDAY, 0);
//				dates[0] = c.getTime();
//				times[0] = c.get(Calendar.YEAR); //올해
//				c.add(Calendar.YEAR, -1);
//				dates[1] = c.getTime();
//				times[1] = c.get(Calendar.YEAR); //작년
//				c.add(Calendar.YEAR, LIMIT_TIME_YEAR);
//				dates[2] = c.getTime();
//				break;
//		}
//	}
		
	
//	/*
//	 * KeywordRankingTask
//	 * .keyword.0/1 파일에 기록된 검색키워드들을 읽어와서 상위N개를 해당 시간 DB table에 insert하는 작업
//	 * */
//	class KeywordRankingTask extends TimerTask{
//		
//		public void run() {
//			//Log logger = LoggerFactory.getLogger(KeywordRankingTask.class);
//			//switch file 0 <-> 1
////			logger.debug("Run KeywordRankingTask");
//			int old = switchFile();
//			//start make ranking
//			
//			MemoryKeyword memoryKeyword = new MemoryKeyword();
//			
//			File file = new File(logPath + old);
////			logger.debug("Log file path = "+file.getAbsolutePath()+" , "+file.length());
//			BufferedReader br = null;
//			try {
//				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//			} catch (FileNotFoundException e) {
//				//logger.error(e.getMessage(),e);
//			} 
//		
//			//for calculating popular rate
//			int totHit = 0;
//			
//			String term = null;
//			try {
//				while(br!=null && (term = br.readLine()) != null){
//					logger.debug("read and put keyword " +term);
//					memoryKeyword.put(term);
//					totHit ++;
//				}
//			} catch (IOException e) {
//				logger.error(e.getMessage(),e);
//			}
//			
//			if(br!=null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					logger.error(e.getMessage(),e);
//				}
//			}
//
//			makePopularKeyword(memoryKeyword, totHit);
//			file.delete();
//			
//		}
//		
//		/*
//		 * 이전 시간의 키워드통계를 가져와 현재시간의 키워드통계와 합산하도록 한다. 
//		 * 계산식은 {{현재키워드인기율}} + {{이전키워드인기율}} * {{보존상수}} 값.
//		 * 인기율은 만분률 정수값을 취하도록 한다.
//		 * 이전시간의 키워드통계를 계속 가져오기 때문에 결과값은 항상 누적이 된다.
//		 * */
//		public void makePopularKeyword(MemoryKeyword memoryKeyword, int totHit) {
//			Calendar scal = Calendar.getInstance();
//			scal.setTimeInMillis(this.scheduledExecutionTime());
////			logger.debug("this.scheduledExecutionTime()= {}",scal);
//			DBService dbHandler = DBService.getInstance();
//			SetDictionary keywordHit = dbHandler.getDAO("KeywordHit");
//			int count = keywordHit.selectCount();
//			
//			
//			//remove old data
//			List<KeywordHitVO> list = null;
//			Iterator<KeywordHitVO> iter = null;
//			
//			if(scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//				//.clearHourOfDay(hourOfDay);
//				keywordHit.clearHit(KeywordHit.POPULAR_HOUR, hourOfDay);
//			}
//			
//			if(totHit > 0) {
//				memoryKeyword.calcPopular(totHit);
//				MemoryKeyword cKeyword = new MemoryKeyword();
//				iter = memoryKeyword.getIterator(MAX_KEYWORD_COUNT);
//				while(iter.hasNext()) {
//					KeywordHitVO kh = iter.next();
//					if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//					if(kh.dateUpdate == null) { kh.dateUpdate = kh.dateRegister; }
//					cKeyword.add(kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.dateRegister.getTime(), kh.dateUpdate.getTime());
//				}
//				list = keywordHit.selectKeywordHit(KeywordHit.POPULAR_HOUR, hourOfDay);
//				keywordHit.clearHit(KeywordHit.POPULAR_HOUR, hourOfDay);
//				for(KeywordHitVO kh : list) {
//					if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//					if(kh.dateUpdate == null) { kh.dateUpdate = kh.dateRegister; }
//					cKeyword.add(kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.dateRegister.getTime(), kh.dateUpdate.getTime());
//					totHit+=kh.hit;
//					//logger.debug("total hit = db : "+totHit+":"+kh.hit+":"+kh.keyword);
//				}
//				cKeyword.calcPopular(totHit);
//				iter = cKeyword.getIterator(MAX_KEYWORD_COUNT);
//				for(int keywordInx=0;iter.hasNext();) {
//					KeywordHitVO kh = iter.next();
//					keywordHit.insert(KeywordHit.POPULAR_HOUR, hourOfDay, kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.isUsed, kh.dateRegister, kh.dateUpdate);
//					int tid = memoryKeyword.getId(kh.keyword);
//					if(tid>=0) { memoryKeyword.setValues(tid, kh.hit, kh.popular, kh.prevRank, kh.dateRegister.getTime(), kh.dateUpdate.getTime()); }
//				}
//				if(scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//					memoryKeyword = cKeyword;
//				}
//			}
//			
//			list = keywordHit.selectKeywordHit(KeywordHit.POPULAR_ACCUM, 0);
//			//logger.debug("calculate keyword "+list.size()+" at hour "+hourOfDay);
//			if(logger.isDebugEnabled()) {
////				logger.debug("calculate keyword "+list.size()+" at "+scal.get(Calendar.HOUR_OF_DAY)+":"+scal.get(Calendar.MINUTE)+":"+scal.get(Calendar.SECOND));
//				iter = memoryKeyword.getIterator(MAX_KEYWORD_COUNT);
//				while(iter.hasNext()) {
//					logger.debug("memory keyword has ["+iter.next()+"]");
//				}
//			}
//	
//			for(int keywordInx=0; keywordInx < list.size(); keywordInx++) {
//				KeywordHitVO kh = list.get(keywordInx);
//				int keywordId = memoryKeyword.getId(kh.keyword);
//				//신규키워드에 대해 0.1 정도의 가중치를 준다.
//				int cpop = memoryKeyword.getPopular(keywordId);
//				if(cpop!=0) { kh.popular = (int)((kh.popular * 0.4) + memoryKeyword.getPopular(keywordId) * 0.6); }
//				if(scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//					//인기율은 시간에 반비례 하도록 계속 감소시키도록 한다.
//					kh.popular = (int)( kh.popular + (memoryKeyword.getPopular(keywordId) * POPULAR_CONSTANT) );
//					//1시간에 한번씩 랭크 변동추이를 변경한다.
//					kh.prevRank = keywordInx+1;
//				}
//				if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//				memoryKeyword.add(kh.keyword, kh.hit, kh.popular,kh.prevRank,kh.dateRegister.getTime(),kh.dateUpdate.getTime());
//			}
//			
//			iter = memoryKeyword.getIterator(MAX_KEYWORD_COUNT);
//			
//			keywordHit.clearHit(KeywordHit.POPULAR_ACCUM, 0);
//
//			//memoryKeyword 는 이미 정렬된 상태 이므로 상위부터 100개까지만 잘라 데이터베이스에 입력
//			for(int inx = 0; inx < MAX_KEYWORD_COUNT && iter.hasNext(); inx++ ) {
//				KeywordHitVO kh = iter.next();
////				logger.debug("Popular = "+kh.keyword+" = "+kh.hit+"/"+kh.popular);
//				keywordHit.insert(KeywordHit.POPULAR_ACCUM, 0, kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.isUsed, kh.dateRegister, kh.dateUpdate);
//			}
//			
//			//하루한번 통계를 계산하도록 한다.
//			if(scal.get(Calendar.HOUR_OF_DAY)==1 && scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//				makeKeywordStatistics();
//			}
//		}
//
//		/**
//		 * 하루에 한번 씩 통계저장을 수행하는 부분.
//		 */
//		public void makeKeywordStatistics() {
//			Calendar todayCalendar = Calendar.getInstance();
//			DBService dbHandler = DBService.getInstance();
//			
//			Date[] dates = new Date[3];
//			int[] times = new int[2];
//			
//			Date cdate,pdate,ldate,cweek,pweek,lweek,cmonth,pmonth,lmonth,cyear,pyear,lyear;
//			int cidate, pidate, ciweek, piweek, cimonth, pimonth, ciyear, piyear;
//			
//			calculateDate(KeywordHit.STATISTICS_DATE,todayCalendar,dates,times);
//			cdate = dates[0];
//			pdate = dates[1];
//			ldate = dates[2];
//			cidate = times[0];
//			pidate = times[1];
//			
//			calculateDate(KeywordHit.STATISTICS_WEEK,todayCalendar,dates,times);
//			cweek = dates[0];
//			pweek = dates[1];
//			lweek = dates[2];
//			ciweek = times[0];
//			piweek = times[1];
//			
//			calculateDate(KeywordHit.STATISTICS_MONTH,todayCalendar,dates,times);
//			cmonth = dates[0];
//			pmonth = dates[1];
//			lmonth = dates[2];
//			cimonth = times[0];
//			pimonth = times[1];
//			
//			calculateDate(KeywordHit.STATISTICS_YEAR,todayCalendar,dates,times);
//			cyear = dates[0];
//			pyear = dates[1];
//			lyear = dates[2];
//			ciyear = times[0];
//			piyear = times[1];
//			
//			MemoryKeyword currentKeyword = new MemoryKeyword();
//			//현재데이터를 구함
//			List<KeywordHit>clist = keywordHit.selectKeywordHit(KeywordHit.POPULAR_ACCUM, 0);
//			for(int keywordInx=0; keywordInx < clist.size(); keywordInx++) {
//				KeywordHitVO kh = clist.get(keywordInx);
//				if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//				currentKeyword.add(kh.keyword, kh.hit, kh.popular,keywordInx+1, kh.dateRegister.getTime(), kh.dateUpdate.getTime());
//			}
//			
//			logger.debug("calculating statistics for date..."+cdate);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordHit.STATISTICS_DATE,pidate,cidate,pdate,cdate,ldate);
//			logger.debug("calculating statistics for week..."+cweek);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordHit.STATISTICS_WEEK,piweek,ciweek,pweek, cweek,lweek);
//			logger.debug("calculating statistics for month..."+cmonth);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordHit.STATISTICS_MONTH,pimonth,cimonth,pmonth,cmonth,lmonth);
//			logger.debug("calculating statistics for year..."+cyear);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordHit.STATISTICS_YEAR,piyear,ciyear,pyear,cyear,lyear);
//		}
//		
//		public void makeKeywordStatistics(DBService dbHandler, MemoryKeyword currentKeyword, int type, int ptime, int ctime, Date pdate, Date cdate, Date limit) {
//			//이전데이터를 구함
//			MemoryKeyword prevKeyword = new MemoryKeyword();
//			List<KeywordHit>list = keywordHit.selectKeywordHit(type,ptime,pdate);
//			for(int keywordInx=0; keywordInx < list.size(); keywordInx++) {
//				KeywordHitVO kh = list.get(keywordInx);
//				if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//				prevKeyword.put(kh.keyword, kh.hit, kh.popular,keywordInx+1, kh.dateRegister.getTime(), kh.dateUpdate.getTime());//이전랭크정보를 저장
//			}
//			Iterator<KeywordHitVO> iter = currentKeyword.getIterator(MAX_KEYWORD_COUNT);
//			keywordHit.clearHit(type, ctime, cdate);
//			for(int inx = 0; inx < MAX_KEYWORD_COUNT && iter.hasNext(); inx++ ) {
//				KeywordHitVO kh = iter.next();
//				//예전랭크정보와의 비교정보만 구하여 저장하도록 한다.
//				int prevRank = prevKeyword.getRank(kh.keyword);
//				if(prevRank == 0) { prevRank = 999; } //999는 신규키워드, 그렇지 않은 경우에는 예전 랭크를 그대로 가져다 쓴다.
//				keywordHit.insert(type, ctime, kh.keyword, kh.hit, kh.popular, prevRank, kh.isUsed, cdate, kh.dateUpdate);
//				keywordHit.clearHitBefore(type, ctime, limit);
//				if(logger.isDebugEnabled()) {
//					logger.debug("updating keyword ["+kh.keyword+"] rank = "+inx+" prevrank = "+prevRank + " type = "+type+" / "+ptime+" date = "+cdate);
//					logger.debug("deleting keyword type = "+type+"/"+ptime+" date before "+limit);
//				}
//			}
//		}
//	}
//	
//	/*
//	 * KeywordFailRankingTask
//	 * .keyword.fail.0/1 파일에 기록된 실패키워드들을 읽어와서 상위N개를 해당 시간 DB table에 insert하는 작업
//	 * */
//	class KeywordFailRankingTask extends TimerTask{
//		
//		public void run() {
//			//switch file 0 <-> 1
////			logger.debug("Run KeywordFailRankingTask");
//			int old = switchFileFail();
//			//start make ranking
//			
//			MemoryKeyword memoryKeyword = new MemoryKeyword();
//			
//			File file = new File(logPathFail + old);
////			logger.debug("Log fail file path = "+file.getAbsolutePath()+" , "+file.length());
//			BufferedReader br = null;
//			try {
//				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//			} catch (FileNotFoundException e) {
//				//logger.error(e.getMessage(),e);
//			} 
//		
//			//for calculating popular rate
//			int totHit = 0;
//			
//			String term = null;
//			try {
//				while(br!=null && (term = br.readLine()) != null){
//					logger.debug("read and put fail keyword " +term);
//					memoryKeyword.put(term);
//					totHit ++;
//				}
//			} catch (IOException e) {
//				logger.error(e.getMessage(),e);
//			}
//			
//			if(br!=null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					logger.error(e.getMessage(),e);
//				}
//			}
//
//			makePopularKeyword(memoryKeyword, totHit);
//			file.delete();
//			
//		}
//		
//		/*
//		 * 이전 시간의 키워드통계를 가져와 현재시간의 키워드통계와 합산하도록 한다. 
//		 * 계산식은 {{현재키워드인기율}} + {{이전키워드인기율}} * {{보존상수}} 값.
//		 * 인기율은 만분률 정수값을 취하도록 한다.
//		 * 이전시간의 키워드통계를 계속 가져오기 때문에 결과값은 항상 누적이 된다.
//		 * */
//		public void makePopularKeyword(MemoryKeyword memoryKeyword, int totHit) {
//			Calendar scal = Calendar.getInstance();
//			scal.setTimeInMillis(this.scheduledExecutionTime());
//			
//			DBService dbHandler = DBService.getInstance();
//
//			//remove old data
//			List<KeywordFail> list = null;
//			Iterator<KeywordHitVO> iter = null;
//			
//			if(scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//				//.clearHourOfDay(hourOfDay);
//				dbHandler.KeywordFail.clearFail(KeywordFail.POPULAR_HOUR, hourOfDay);
//			}
//			
//			if(totHit > 0) {
//				memoryKeyword.calcPopular(totHit);
//				MemoryKeyword cKeyword = new MemoryKeyword();
//				iter = memoryKeyword.getIteratorFail(MAX_KEYWORD_COUNT);
//				while(iter.hasNext()) {
//					KeywordHitVO kh = iter.next();
//					if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//					if(kh.dateUpdate == null) { kh.dateUpdate = kh.dateRegister; }
//					cKeyword.add(kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.dateRegister.getTime(), kh.dateUpdate.getTime());
//				}
//				list = dbHandler.KeywordFail.selectKeywordFail(KeywordFail.POPULAR_HOUR, hourOfDay);
//				dbHandler.KeywordFail.clearFail(KeywordFail.POPULAR_HOUR, hourOfDay);
//				for(KeywordHitVO kh : list) {
//					if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//					if(kh.dateUpdate == null) { kh.dateUpdate = kh.dateRegister; }
//					cKeyword.add(kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.dateRegister.getTime(), kh.dateUpdate.getTime());
//					totHit+=kh.hit;
//					//logger.debug("total hit = db : "+totHit+":"+kh.hit+":"+kh.keyword);
//				}
//				cKeyword.calcPopular(totHit);
//				iter = cKeyword.getIteratorFail(MAX_KEYWORD_COUNT);
//				for(int keywordInx=0;iter.hasNext();) {
//					KeywordHitVO kh = iter.next();
//					dbHandler.KeywordFail.insert(KeywordFail.POPULAR_HOUR, hourOfDay, kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.isUsed, kh.dateRegister, kh.dateUpdate);
//					int tid = memoryKeyword.getId(kh.keyword);
//					if(tid>=0) { memoryKeyword.setValues(tid, kh.hit, kh.popular, kh.prevRank, kh.dateRegister.getTime(), kh.dateUpdate.getTime()); }
//				}
//				if(scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//					memoryKeyword = cKeyword;
//				}
//			}
//			
//			list = dbHandler.KeywordFail.selectKeywordFail(KeywordFail.POPULAR_ACCUM, 0);
//			//logger.debug("calculate keyword "+list.size()+" at hour "+hourOfDay);
//			if(logger.isDebugEnabled()) {
////				logger.debug("calculate keyword "+list.size()+" at "+scal.get(Calendar.HOUR_OF_DAY)+":"+scal.get(Calendar.MINUTE)+":"+scal.get(Calendar.SECOND));
//				iter = memoryKeyword.getIteratorFail(MAX_KEYWORD_COUNT);
//				while(iter.hasNext()) {
//					logger.debug("memory keyword has ["+iter.next()+"]");
//				}
//			}
//	
//			for(int keywordInx=0; keywordInx < list.size(); keywordInx++) {
//				KeywordHitVO kh = list.get(keywordInx);
//				int keywordId = memoryKeyword.getId(kh.keyword);
//				//신규키워드에 대해 0.1 정도의 가중치를 준다.
//				int cpop = memoryKeyword.getPopular(keywordId);
//				if(cpop!=0) { kh.popular = (int)((kh.popular * 0.4) + memoryKeyword.getPopular(keywordId) * 0.6); }
//				if(scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//					//인기율은 시간에 반비례 하도록 계속 감소시키도록 한다.
//					kh.popular = (int)( kh.popular + (memoryKeyword.getPopular(keywordId) * POPULAR_CONSTANT) );
//					//1시간에 한번씩 랭크 변동추이를 변경한다.
//					kh.prevRank = keywordInx+1;
//				}
//				if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//				memoryKeyword.add(kh.keyword, kh.hit, kh.popular,kh.prevRank,kh.dateRegister.getTime(),kh.dateUpdate.getTime());
//			}
//			
//			iter = memoryKeyword.getIteratorFail(MAX_KEYWORD_COUNT);
//			
//			dbHandler.KeywordFail.clearFail(KeywordFail.POPULAR_ACCUM, 0);
//
//			//memoryKeyword 는 이미 정렬된 상태 이므로 상위부터 100개까지만 잘라 데이터베이스에 입력
//			for(int inx = 0; inx < MAX_KEYWORD_COUNT && iter.hasNext(); inx++ ) {
//				KeywordHitVO kh = iter.next();
////				logger.debug("Popular = "+kh.keyword+" = "+kh.hit+"/"+kh.popular);
//				dbHandler.KeywordFail.insert(KeywordFail.POPULAR_ACCUM, 0, kh.keyword, kh.hit, kh.popular, kh.prevRank, kh.isUsed, kh.dateRegister, kh.dateUpdate);
//			}
//			
//			//하루한번 통계를 계산하도록 한다.
//			if(scal.get(Calendar.HOUR_OF_DAY)==1 && scal.get(Calendar.MINUTE)==0 && scal.get(Calendar.SECOND)==0) {
//				makeKeywordStatistics();
//			}
//		}
//
//		/**
//		 * 하루에 한번 씩 통계저장을 수행하는 부분.
//		 */
//		public void makeKeywordStatistics() {
//			Calendar todayCalendar = Calendar.getInstance();
//			DBService dbHandler = DBService.getInstance();
//			
//			Date[] dates = new Date[3];
//			int[] times = new int[2];
//			
//			Date cdate,pdate,ldate,cweek,pweek,lweek,cmonth,pmonth,lmonth,cyear,pyear,lyear;
//			int cidate, pidate, ciweek, piweek, cimonth, pimonth, ciyear, piyear;
//			
//			calculateDate(KeywordFail.STATISTICS_DATE,todayCalendar,dates,times);
//			cdate = dates[0];
//			pdate = dates[1];
//			ldate = dates[2];
//			cidate = times[0];
//			pidate = times[1];
//			
//			calculateDate(KeywordFail.STATISTICS_WEEK,todayCalendar,dates,times);
//			cweek = dates[0];
//			pweek = dates[1];
//			lweek = dates[2];
//			ciweek = times[0];
//			piweek = times[1];
//			
//			calculateDate(KeywordFail.STATISTICS_MONTH,todayCalendar,dates,times);
//			cmonth = dates[0];
//			pmonth = dates[1];
//			lmonth = dates[2];
//			cimonth = times[0];
//			pimonth = times[1];
//			
//			calculateDate(KeywordFail.STATISTICS_YEAR,todayCalendar,dates,times);
//			cyear = dates[0];
//			pyear = dates[1];
//			lyear = dates[2];
//			ciyear = times[0];
//			piyear = times[1];
//			
//			MemoryKeyword currentKeyword = new MemoryKeyword();
//			//현재데이터를 구함
//			List<KeywordFail>clist = dbHandler.KeywordFail.selectKeywordFail(KeywordFail.POPULAR_ACCUM, 0);
//			for(int keywordInx=0; keywordInx < clist.size(); keywordInx++) {
//				KeywordHitVO kh = clist.get(keywordInx);
//				if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//				currentKeyword.add(kh.keyword, kh.hit, kh.popular,keywordInx+1, kh.dateRegister.getTime(), kh.dateUpdate.getTime());
//			}
//			
//			logger.debug("calculating statistics for date..."+cdate);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordFail.STATISTICS_DATE,pidate,cidate,pdate,cdate,ldate);
//			logger.debug("calculating statistics for week..."+cweek);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordFail.STATISTICS_WEEK,piweek,ciweek,pweek, cweek,lweek);
//			logger.debug("calculating statistics for month..."+cmonth);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordFail.STATISTICS_MONTH,pimonth,cimonth,pmonth,cmonth,lmonth);
//			logger.debug("calculating statistics for year..."+cyear);
//			makeKeywordStatistics(dbHandler,currentKeyword,KeywordFail.STATISTICS_YEAR,piyear,ciyear,pyear,cyear,lyear);
//		}
//		
//		public void makeKeywordStatistics(DBService dbHandler, MemoryKeyword currentKeyword, int type, int ptime, int ctime, Date pdate, Date cdate, Date limit) {
//			//이전데이터를 구함
//			MemoryKeyword prevKeyword = new MemoryKeyword();
//			List<KeywordFail>list = dbHandler.KeywordFail.selectKeywordFail(type,ptime,pdate);
//			for(int keywordInx=0; keywordInx < list.size(); keywordInx++) {
//				KeywordHitVO kh = list.get(keywordInx);
//				if(kh.dateRegister == null) { kh.dateRegister = new Date(); }
//				prevKeyword.put(kh.keyword, kh.hit, kh.popular,keywordInx+1, kh.dateRegister.getTime(), kh.dateUpdate.getTime());//이전랭크정보를 저장
//			}
//			Iterator<KeywordHitVO> iter = currentKeyword.getIteratorFail(MAX_KEYWORD_COUNT);
//			dbHandler.KeywordFail.clearFail(type, ctime, cdate);
//			for(int inx = 0; inx < MAX_KEYWORD_COUNT && iter.hasNext(); inx++ ) {
//				KeywordHitVO kh = iter.next();
//				//예전랭크정보와의 비교정보만 구하여 저장하도록 한다.
//				int prevRank = prevKeyword.getRank(kh.keyword);
//				if(prevRank == 0) { prevRank = 999; } //999는 신규키워드, 그렇지 않은 경우에는 예전 랭크를 그대로 가져다 쓴다.
//				dbHandler.KeywordFail.insert(type, ctime, kh.keyword, kh.hit, kh.popular, prevRank, kh.isUsed, cdate, kh.dateUpdate);
//				dbHandler.KeywordFail.clearFailBefore(type, ctime, limit);
//				if(logger.isDebugEnabled()) {
//					logger.debug("updating keyword ["+kh.keyword+"] rank = "+inx+" prevrank = "+prevRank + " type = "+type+" / "+ptime+" date = "+cdate);
//					logger.debug("deleting keyword type = "+type+"/"+ptime+" date before "+limit);
//				}
//			}
//		}
//	}
//	
//	
//	/*
//	 * StreamFlushTask
//	 * */
//	class StreamFlushTask extends TimerTask{
//
//		@Override
//		public void run() {
//			if(keywordWriter!=null) {
//				synchronized(keywordWriter){
//					keywordWriter.flush();
//				}
//			}
//			if(keywordWriterFail!=null) {
//				synchronized(keywordWriterFail){
//					keywordWriterFail.flush();
//				}
//			}
//		}
//	}


	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}

}
