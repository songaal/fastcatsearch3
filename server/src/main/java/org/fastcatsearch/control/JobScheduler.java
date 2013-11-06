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

package org.fastcatsearch.control;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.indexing.CollectionFullIndexingJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*

@Deprecated JobService내에서 scheduledJob을 이용하여 스케쥴링수행. 
*/
public class JobScheduler {
	private static Logger logger = LoggerFactory.getLogger(JobScheduler.class);
	private Timer timer;
	private Map<String,TimerTask> taskMap;
//	private File scheduleFile;
//	private XMLSettingManager<SchedulerSetting> scheduleSettingManager;
	
	
	public JobScheduler() {
	}
	
//	public JobScheduler(File scheduleFile) {
//		this.scheduleFile = scheduleFile;
//		
//	}
//	
//	public void saveSchedule(){
//		scheduleSettingManager.save();
//	}
	
	public void start(){
		try {
			//데몬 쓰레드로 시작해야 메인쓰레드가 종료할때 같이 종료됨.
			load();
		} catch (SettingException e) {
			logger.error("JobScheduler cannot be started!. "+e.getStackTrace(),e);
		}
	}
	
	private void load() throws SettingException{
		timer = new Timer(true);
		taskMap = new HashMap<String, TimerTask>();
		
//		if(scheduleFile != null){
//			scheduleSettingManager = new XMLSettingManager<SchedulerSetting>(scheduleFile, SchedulerSetting.class);
//			taskMap = new HashMap<String, TimerTask>(); 
//			timer = new Timer(true);
//			
//			List<IndexingSchedule> indexingScheduleList = scheduleSettingManager.getSetting().getIndexingScheduleList();
//			for (IndexingSchedule indexingSchedule : indexingScheduleList) {
//				setIndexSchedule(indexingSchedule.getCollectionId(), indexingSchedule.getIndexingType(), indexingSchedule.getStart(), indexingSchedule.getEnd(), indexingSchedule.getPeriodInSecond(), indexingSchedule.isActive());
//				logger.debug("[IndexingSchedule register] {} / {}", indexingSchedule.getCollectionId(), indexingSchedule.getIndexingType());
//			}
//			List<JobSchedule> jobScheduleList = scheduleSettingManager.getSetting().getJobScheduleList();
//			for (JobSchedule jobSchedule : jobScheduleList) {
//				//TODO
//			}
//		}
	}
	
	private String getIndexingKey(String collection, IndexingType type){
		return collection + "/"+type.name();
	}
	
	public boolean setIndexSchedule(String collectionId, IndexingType indexingType, String start, String end, int period, boolean isActive) {
		if(indexingType == IndexingType.FULL){
			String key = getIndexingKey(collectionId, indexingType);
			String className = CollectionFullIndexingJob.class.getClass().getName(); 
			String args = collectionId;
			schedule(key, className, args, start, end, period, isActive);
		}else if(indexingType == IndexingType.ADD){
			String key = getIndexingKey(collectionId, indexingType);
			//FIXME
			String className = CollectionFullIndexingJob.class.getClass().getName();
			String args = collectionId;
			schedule(key, className, args, start, end, period, isActive);
		}else{
			return false;
		}
		return true;
	}
	
//	public boolean setJobSchedule(String key, String jobClassName, String args, Timestamp startTime, int period, boolean isActive) {
//		schedule(key, jobClassName, args, startTime, period, isActive);
//		return true;
//	}
	
	private void schedule(String key, String jobClassName, String args, String start, String end, int period, boolean isActive) {
		TimerTask task = new JobTask(jobClassName, args);
		TimerTask oldTask = taskMap.put(key, task);
		
		if(oldTask != null){
			oldTask.cancel();
		}
		
		long startTime = 0;
		long endTime = 0;
		try {
			startTime = Formatter.parseDate(start).getTime();
		} catch (ParseException e) {
			logger.error("", e);
		}
		try {
			endTime = Formatter.parseDate(end).getTime();
		} catch (ParseException e) {
			logger.error("", e);
		}
		
		if(isActive){
			if(startTime >= System.currentTimeMillis()){
				timer.scheduleAtFixedRate(task, new Date(startTime), period * 1000L);
			}else{
				//avoid catch up of scheduleAtFixedRate
				long newFirstTime = startTime;
				while(newFirstTime < System.currentTimeMillis())
					newFirstTime += (period * 1000L); //increase by period
				logger.debug("newFirstTime = "+new Date(newFirstTime)+" period="+period+", task ="+task);
				timer.scheduleAtFixedRate(task, new Date(newFirstTime), period * 1000L);
			}
		}
		logger.debug("timer = "+timer);
	}
	
	public boolean reloadIndexingSchedule(String collectionId, IndexingType indexingType, boolean isActive){
		String indexingKey = getIndexingKey(collectionId, indexingType);
//		if(isActive){
//			List<IndexingSchedule> indexingScheduleList = scheduleSettingManager.getSetting().getIndexingScheduleList();
//			for (IndexingSchedule indexingSchedule : indexingScheduleList) {
//				if(indexingSchedule.getCollectionId().equals(collectionId)){
//					setIndexSchedule(indexingSchedule.getCollectionId(), indexingSchedule.getIndexingType(), indexingSchedule.getStart(), indexingSchedule.getEnd(), indexingSchedule.getPeriodInSecond(), indexingSchedule.isActive());
//					logger.debug("[IndexingSchedule register] {} / {}", indexingSchedule.getCollectionId(), indexingSchedule.getIndexingType());
//				}
//			}
//		}else{
//			TimerTask oldTask = taskMap.remove(indexingKey);
//			if(oldTask != null){
//				oldTask.cancel();
//			}
//		}
		return false;
	}
	
	public boolean reload() throws SettingException{
		stop();
		load();
		return true;
	}
	
	public void stop(){
		timer.cancel();
		taskMap.clear();
	}
	
	class JobTask extends TimerTask{
		private Job job;
		
		public JobTask(String jobClassName, String args){
			job = DynamicClassLoader.loadObject(jobClassName, Job.class);
			String[] arglist = args.split(" ");
			job.setArgs(arglist);
			job.setScheduled(true); //scheduled job.
		}
		
		public void run(){
			ResultFuture jobResult = null;
			
			try {
				jobResult = ServiceManager.getInstance().getService(JobService.class).offer(job);
//				jobResult = JobController.getInstance().offer(job);
			} catch (NullPointerException e) {
				logger.error(e.getMessage(),e);
			}
			
			if(jobResult == null){
				//ignore
				logger.debug("jobResult = "+jobResult);
				logger.debug("Scheduled job "+job+" is ignored.");
			}else{
				logger.debug("jobResult = "+jobResult);
				Object result = jobResult.take();
				logger.debug("Schedule Job Result = "+result);
			}
		}
	}
	
}
