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

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingSchedule;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JobScheduler {
	private static Logger logger = LoggerFactory.getLogger(JobScheduler.class);
	private Timer timer;
	private Map<String,TimerTask> taskMap;
	
	public JobScheduler() {
//		props = IRSettings.getSchedule(true);
//		timer = new Timer();
		taskMap = new HashMap<String, TimerTask>(); 
	}
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
		//Indexing Schedule
		List<IndexingSchedule> schedules = DBService.getInstance().IndexingSchedule.selectAll();
		int schedulesSize = schedules.size(); 
		for (int i = 0; i < schedulesSize; i++) {
			IndexingSchedule schedule = schedules.get(i);
			setIndexSchedule(schedule.collection, schedule.type, schedule.startTime, schedule.period, schedule.isActive);
			logger.debug("[Job Load]" + schedule.collection + "/" + schedule.type);
		}		
		
	}
	
	private String getKey(String collection, String type){
		if(type.equalsIgnoreCase("F")){
			return collection + ".FULL";
		}else if(type.equalsIgnoreCase("I")){
			return collection + ".INC";
		}
		return null;
	}
	
	public boolean setIndexSchedule(String collection, String type, Timestamp startTime, int period, boolean isActive) {
		if(type.equalsIgnoreCase("F")){
			String key = getKey(collection, type);
			String className = "org.fastcatsearch.job.FullIndexJob"; 
			String args = collection;
			schedule(key, className, args, startTime, period, isActive);
		}else if(type.equalsIgnoreCase("I")){
			String key = getKey(collection, type);
			String className = "org.fastcatsearch.job.IncIndexJob"; 
			String args = collection;
			schedule(key, className, args, startTime, period, isActive);
		}else{
			return false;
		}
		return true;
	}
	public boolean setSchedule(String key, String jobClassName, String args, Timestamp startTime, int period, boolean isActive) {
		schedule(key, jobClassName, args, startTime, period, isActive);
		return true;
	}
	
	private void schedule(String key, String jobClassName, String args, Timestamp startTime, int period, boolean isActive) {
		TimerTask task = new JobTask(jobClassName, args);
		TimerTask oldTask = taskMap.put(key, task);
		
		if(oldTask != null){
			oldTask.cancel();
		}
		
		if(isActive){
			if(startTime.getTime() >= System.currentTimeMillis()){
				timer.scheduleAtFixedRate(task, new Date(startTime.getTime()), period * 1000L);
			}else{
				//avoid catch up of scheduleAtFixedRate
				long newFirstTime = startTime.getTime();
				while(newFirstTime < System.currentTimeMillis())
					newFirstTime += (period * 1000L); //increase by period
				logger.debug("newFirstTime = "+new Date(newFirstTime)+" period="+period+", task ="+task);
				timer.scheduleAtFixedRate(task, new Date(newFirstTime), period * 1000L);
			}
		}
		logger.debug("timer = "+timer);
	}
	
	public boolean reloadIndexingSchedule(String collection, String type, boolean isActive){
		if(isActive){
			IndexingSchedule schedules = DBService.getInstance().IndexingSchedule.select(collection, type);
			setIndexSchedule(schedules.collection, schedules.type, schedules.startTime, schedules.period, schedules.isActive);
		}else{
			String key = getKey(collection, type);
			TimerTask oldTask = taskMap.remove(key);
			if(oldTask != null){
				oldTask.cancel();
			}
		}
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
		
		@SuppressWarnings("unused")
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
