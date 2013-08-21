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

package org.fastcatsearch.management;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.SystemMonitoringInfo;
import org.fastcatsearch.db.dao.SystemMonitoringInfoMinute;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;



public class ManagementInfoService extends AbstractService{
	
	private static long PERIOD = 1000; //1초마다 InfoCheckerTask를 수행한다.
	private static long START_DELAY = 100;
	private static long PERIOD_1MIN = 1000 * 60; //1분 주기.
	private static long PERIOD_1HOU = 1000 * 60 * 60; //1시간 주기.
	private static long PERIOD_1DAY = 1000 * 60 * 60 * 24; //1시간 주기.
	
	private ManagementInfoHandler handler;
	private Timer timer;
	
	private JvmCpuInfo jvmCpuInfoPerSecond = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerSecond = new JvmMemoryInfo();
	private JvmCpuInfo jvmCpuInfoPerMinute = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerMinute = new JvmMemoryInfo();
	private JvmCpuInfo jvmCpuInfoPerHour = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerHour = new JvmMemoryInfo();
	private JvmCpuInfo jvmCpuInfoPerDay = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerDay = new JvmMemoryInfo();
	private JvmCpuInfo jvmCpuInfoPerMonth = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerMonth = new JvmMemoryInfo();
	
	private int countPerMinute;//1분에 몇번의 초별 계산을 했는가.? 대부분 60이 나와야 한다. 
	private int countPerHour;//1시간에 몇번의 분별 계산을 했는가.? 대부분 60이 나와야 한다. 
	private int countPerDay;//1일에 몇번의 시별 계산을 했는가.? 대부분 24가 나와야 한다. 
	private int countPerMonth;//1달에 몇번의 일별 계산을 했는가.? 대부분 60이 나와야 한다.
	
	private static ManagementInfoService instance;
	
	public static ManagementInfoService getInstance(){
		return instance;
	}
	
	public ManagementInfoService(Environment environment, Settings settings, ServiceManager serviceManager){
		super(environment, settings, serviceManager);
	}
	
	
	public boolean isJvmCpuInfoSupported(){
		return handler.isJvmCpuInfoSupported();
	}
	public boolean isSystemCpuInfoSupported(){
		return handler.isSystemCpuInfoSupported();
	}
	public boolean isLoadAvgInfoSupported(){
		return handler.isLoadAvgInfoSupported();
	}
	public boolean isJvmMemoryInfoSupported(){
		return handler.isJvmMemoryInfoSupported();
	}
	
	public JvmCpuInfo getJvmCpuInfo(){
		return jvmCpuInfoPerSecond;
	}
	public JvmMemoryInfo getJvmMemoryInfo(){
		return jvmMemoryInfoPerSecond;
	}
//	public JvmCpuInfo getJvmCpuInfoPerMinute(){
//		return jvmCpuInfoPerMinute;
//	}
//	public JvmMemoryInfo getJvmMemoryInfoPerMinute(){
//		return jvmMemoryInfoPerMinute;
//	}
	
	class InfoCheckerPerSecondTask extends TimerTask{

		@Override
		public void run() {
			handler.checkJvmCpuInfo(jvmCpuInfoPerSecond);
			handler.checkJvmMemoryInfo(jvmMemoryInfoPerSecond);
			
			//MINUTE별 정보에 더해준다.
			jvmCpuInfoPerMinute.add(jvmCpuInfoPerSecond);
//			jvmCpuInfoPerMinute.print();
			jvmMemoryInfoPerMinute.add(jvmMemoryInfoPerSecond);
			countPerMinute++;
		}
	}
	
	class InfoCheckerPerMinuteTask extends TimerTask{

		@Override
		public void run() {
			if(countPerMinute == 0)
				countPerMinute = 1;
			
			logger.debug("시스템 정보 1분 통계. countPerMinute = {}", countPerMinute);
			JvmCpuInfo cpuInfo = jvmCpuInfoPerMinute.getAverage(countPerMinute);
			JvmMemoryInfo memoryInfo = jvmMemoryInfoPerMinute.getAverage(countPerMinute);
			cpuInfo.print();
			memoryInfo.print();
			countPerMinute = 0;
			
			//DB에 저장하기.
			Timestamp when = new Timestamp(System.currentTimeMillis());
			DBService.getInstance().db().getDAO("SystemMonitoringInfoMinute", SystemMonitoringInfoMinute.class).insert(cpuInfo.jvmCpuUse, memoryInfo.usedHeapMemory + memoryInfo.usedNonHeapMemory, cpuInfo.systemLoadAverage, when);
//			DBHandler.getInstance().commitMon();
			
			jvmCpuInfoPerHour.add(cpuInfo);
			jvmMemoryInfoPerHour.add(memoryInfo);
			countPerHour++;
		}
	}
	
	class InfoCheckerPerHourTask extends TimerTask{

		@Override
		public void run() {
			/*
			 * 1. 시간마다 시간별 통계내기
			 * */
			if(countPerHour == 0)
				countPerHour = 1;
			
			logger.debug("시스템 정보 1시간 통계. countPerHour = {}", countPerHour);
			JvmCpuInfo cpuInfo = jvmCpuInfoPerHour.getAverage(countPerHour);
			JvmMemoryInfo memoryInfo = jvmMemoryInfoPerHour.getAverage(countPerHour);
			cpuInfo.print();
			memoryInfo.print();
			countPerHour = 0;
			
			//DB에 저장하기.
			Timestamp when = new Timestamp(System.currentTimeMillis());
			DBService.getInstance().db().getDAO("SystemMonitoringInfo", SystemMonitoringInfo.class).insert(cpuInfo.jvmCpuUse, memoryInfo.usedHeapMemory + memoryInfo.usedNonHeapMemory, cpuInfo.systemLoadAverage, when, "h");
//			DBHandler.getInstance().commitMon();
			
			jvmCpuInfoPerDay.add(cpuInfo);
			jvmMemoryInfoPerDay.add(memoryInfo);
			countPerDay++;
			
		}
	}
	
	class InfoCheckerPerDayTask extends TimerTask{

		@Override
		public void run() {
			/*
			 * 1. 하루마다 일별 통계내기
			 * */
			if(countPerDay == 0)
				countPerDay = 1;
			
			logger.debug("시스템 정보 1일 통계. countPerDay = {}", countPerDay);
			JvmCpuInfo cpuInfo = jvmCpuInfoPerDay.getAverage(countPerDay);
			JvmMemoryInfo memoryInfo = jvmMemoryInfoPerDay.getAverage(countPerDay);
			cpuInfo.print();
			memoryInfo.print();
			countPerDay = 0;
			
			//DB에 저장하기.
			Timestamp when = new Timestamp(System.currentTimeMillis());
			DBService.getInstance().db().getDAO("SystemMonitoringInfo", SystemMonitoringInfo.class).insert(cpuInfo.jvmCpuUse, memoryInfo.usedHeapMemory + memoryInfo.usedNonHeapMemory, cpuInfo.systemLoadAverage, when, "d");
			
			// 매월 1일 전달 통계저장하기.
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int day = calendar.get(Calendar.DATE);
			if (day == 1) {
				JvmCpuInfo cpuInfo_m = jvmCpuInfoPerMonth.getAverage(countPerMonth);
				JvmMemoryInfo memoryInfo_m = jvmMemoryInfoPerMonth.getAverage(countPerMonth);
				cpuInfo_m.print();
				memoryInfo_m.print();
				countPerMonth = 0;
				
				//DB에 저장하기.
				Timestamp when_m = new Timestamp(System.currentTimeMillis());
				DBService.getInstance().db().getDAO("SystemMonitoringInfo", SystemMonitoringInfo.class).insert(cpuInfo_m.jvmCpuUse, memoryInfo_m.usedHeapMemory + memoryInfo_m.usedNonHeapMemory, cpuInfo_m.systemLoadAverage, when_m, "m");
				
			} 
			
			jvmCpuInfoPerMonth.add(cpuInfo);
			jvmMemoryInfoPerMonth.add(memoryInfo);
			countPerMonth++;
			
			/*
			 * 2. 이전 데이터 지워주기.
			 * */
			DBService.getInstance().db().getDAO("SystemMonitoringInfo", SystemMonitoringInfo.class).deleteOld(1);//1달 이전은 삭제 
			
//			DBHandler.getInstance().commitMon();
		}
	}
	
	@Override
	protected boolean doStart() throws FastcatSearchException {
		
		handler = ManagementInfoHandler.getInstance();
		logger.info("isCpuInfoSupported = {}", isJvmCpuInfoSupported() || isSystemCpuInfoSupported());
		logger.info("isLoadAvgInfoSupported = {}", isLoadAvgInfoSupported());
		
		
		timer = new Timer(true);
		timer.schedule(new InfoCheckerPerSecondTask(), START_DELAY, PERIOD);
		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.SECOND, 0);
		startTime.add(Calendar.MINUTE, 1);
		//다음 minute의 0초에 시작한다.
		timer.schedule(new InfoCheckerPerMinuteTask(), startTime.getTime(), PERIOD_1MIN);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.add(Calendar.HOUR, 1);
		timer.schedule(new InfoCheckerPerHourTask(), startTime.getTime(), PERIOD_1HOU);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.HOUR, 0);
		startTime.add(Calendar.DATE, 1);
		timer.schedule(new InfoCheckerPerDayTask(), startTime.getTime(), PERIOD_1DAY);
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		timer.cancel();
		timer = null;
		return true;
	}
	
	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}
}
