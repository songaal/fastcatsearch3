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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.fastcatsearch.db.DBHandler;
import org.fastcatsearch.db.object.IndexingResult;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SearchJob;
import org.fastcatsearch.job.result.JobResultIndex;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.CatServiceComponent;
import org.fastcatsearch.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author sangwook.song
 *
 */

public class JobController extends CatServiceComponent{
	private static Logger logger = LoggerFactory.getLogger(JobController.class);
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	private BlockingQueue<Job> jobQueue; 
	private Map<Long, JobResult> resultMap;
	private Map<Long, Job> runningJobList;
	private AtomicLong jobId;
	
	private static JobController instance;
	private ThreadPoolExecutor jobExecutor;
	private JobControllerWorker worker;
	private JobScheduler jobScheduler;
	private IndexingMutex indexingMutex;
	
	public static JobController getInstance(){
		if(instance == null)
			instance = new JobController();
		return instance;
	}
	public Collection<Job> getRunningJobs(){
		return runningJobList.values();
	}
	public Collection<String> getIndexingList(){
		return indexingMutex.getIndexingList();
	}
	public void setSchedule(String key, String jobClassName, String args, Timestamp startTime, int period, boolean isYN) throws SettingException{
//		if(!isManager) 
//			return;
		
		jobScheduler.setSchedule(key, jobClassName, args, startTime, period, isYN);
	}
	
	public boolean reloadSchedules() throws SettingException{
//		if(!isManager) 
//			return false;
		
		return jobScheduler.reload();
	}
	
	public boolean toggleIndexingSchedule(String collection, String type, boolean isActive){
//		if(!isManager) 
//			return false;
		
		return jobScheduler.reloadIndexingSchedule(collection, type, isActive);
	}
	public ThreadPoolExecutor getJobExecutor(){
		return jobExecutor;
	}
	
	public JobResult offer(Job job) {
		if(job instanceof FullIndexJob || job instanceof IncIndexJob){
			if(indexingMutex.isLocked(job)){
				indexingLogger.info("The collection ["+job.getStringArgs(0)+"] has already started an indexing job.");
				return null;
			}
		}
		
		long myJobId = jobId.getAndIncrement();
		logger.debug("### OFFER Job-"+myJobId);
		
		if(job instanceof FullIndexJob || job instanceof IncIndexJob){
			indexingMutex.access(myJobId, job);
			DBHandler dbHandler = DBHandler.getInstance();
			String collection = job.getStringArgs(0);
			logger.debug("job="+job+", "+collection);
			
			String indexingType = "-";
			if(job instanceof FullIndexJob)
				indexingType = "F";
			else if(job instanceof IncIndexJob)
				indexingType = "I";
			
			dbHandler.IndexingResult.update(collection, indexingType, IndexingResult.STATUS_RUNNING, -1, -1, -1, job.isScheduled(), new Timestamp(System.currentTimeMillis()), null, 0);
//			dbHandler.commit();
		}
//		job.setId(myJobId);
//		jobQueue.offer(job);
//	
//		return jobResult;
		
		if(job.isNoResult()){
			job.setId(myJobId);
			jobQueue.offer(job);
			return null;
		}else{
			JobResult jobResult = new JobResult();
			resultMap.put(myJobId, jobResult);
			job.setId(myJobId);
			jobQueue.offer(job);
			return jobResult;
		}
	}
	
	public void result(long jobId, Job job, Object result, boolean isSuccess, long st, long et) {
//		if(job.isNoResult()) 
//			return;
		
//		indexingMutex.release(jobId);
		JobResult jobResult = resultMap.remove(jobId);
		runningJobList.remove(jobId);
		logger.debug("### JobResult = {} / map={} / result={} / success= {}", new Object[]{jobResult, resultMap.size(), result, isSuccess});
//		if(isManager){
			if(!(job instanceof SearchJob) || !isSuccess){
				DBHandler dbHandler = DBHandler.getInstance();
				String jobArgs = "";
				if(job.getArgs() != null){
					String[] args = job.getStringArrayArgs();
					for (int i = 0; i < args.length; i++) {
						jobArgs += (args[i] + " ");
					}
				}
				
				String resultStr = "";
				if(result != null){
					resultStr = result.toString();
				}
				dbHandler.JobHistory.insert(jobId, job.getClass().getName(), jobArgs, isSuccess, resultStr, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
//				dbHandler.commit();
			}
			
		
		
			if(job instanceof FullIndexJob || job instanceof IncIndexJob){
				indexingMutex.release(jobId);
				DBHandler dbHandler = DBHandler.getInstance();
				String collection = job.getStringArgs(0);
				logger.debug("job="+job+", "+collection);
				
				String indexingType = "-";
				if(job instanceof FullIndexJob){
					indexingType = "F";
					//전체색인시는 증분색인 정보를 클리어해준다.
					dbHandler.IndexingResult.delete(collection, "I");
				}else if(job instanceof IncIndexJob){
					indexingType = "I";
				}
				int status = isSuccess ? IndexingResult.STATUS_SUCCESS : IndexingResult.STATUS_FAIL;
				if(result instanceof JobResultIndex){
					JobResultIndex jobResultIndex = (JobResultIndex)result; 
					dbHandler.IndexingResult.updateOrInsert(collection, indexingType, status, jobResultIndex.docSize, jobResultIndex.updateSize, jobResultIndex.deleteSize, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
					dbHandler.IndexingHistory.insert(collection, indexingType, isSuccess, jobResultIndex.docSize, jobResultIndex.updateSize, jobResultIndex.deleteSize, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
				}else{
					dbHandler.IndexingResult.updateOrInsert(collection, indexingType, status, 0, 0, 0, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
					dbHandler.IndexingHistory.insert(collection, indexingType, isSuccess, 0, 0, 0, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
				}
//				dbHandler.commit();
			}else if(job instanceof SearchJob){
				//검색소요시간이 길경우 이벤트내역에 warning을 해준다.
				
				if(et - st > 5000){
					SearchJob searchJob = (SearchJob)job;
					String queryString = searchJob.getStringArrayArgs()[0];
					EventDBLogger.warn(EventDBLogger.CATE_SEARCH, "검색처리시간 초과. 소요시간 = "+(et - st)+"ms", queryString);
				}
			}
//		}
		
		if(result != null){
			if(jobResult != null){
				try {
					logger.debug("jobResult.put("+result+")");
					jobResult.put(result, isSuccess);
				} catch (InterruptedException e) { }
			}else{
				logger.info("cannot find where to send a result.");
			}
			
		}else{
			logger.warn("Job-"+jobId+" has no return value.");
			if(jobResult != null){
				try {
					jobResult.put(new Object(), isSuccess);
				} catch (InterruptedException e) { }
			}
		}
		
	}
	
	protected boolean start0() throws ServiceException {
		jobId = new AtomicLong();
		resultMap = new ConcurrentHashMap<Long, JobResult>();
		runningJobList = new ConcurrentHashMap<Long, Job>();
		jobQueue = new LinkedBlockingQueue<Job>();
		indexingMutex = new IndexingMutex();
//		if(isManager){
			jobScheduler = new JobScheduler();
//		}
		
		IRConfig irconfig = IRSettings.getConfig();
		int executorCorePoolSize = irconfig.getInt("jobExecutor.core.poolsize");
		int executorMaxPoolSize = irconfig.getInt("jobExecutor.max.poolsize");
		int executorKeepAliveTime = irconfig.getInt("jobExecutor.keepAliveTime");
		jobExecutor = new ThreadPoolExecutor(executorCorePoolSize, executorMaxPoolSize
				, executorKeepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(executorMaxPoolSize), new ThreadPoolExecutor.CallerRunsPolicy());
//		try{
		worker = new JobControllerWorker();
		worker.start();
//		}catch(IllegalThreadStateException e){
//			worker = new JobControllerWorker();
//			worker.start();
//		}
		jobScheduler.start();
		logger.debug("JobController started!");
		
		return true;
	}
	
	protected boolean shutdown0() {
		logger.debug("JobController shutdown requested.");
		worker.interrupt();
		resultMap.clear();
		jobQueue.clear();
		runningJobList.clear();
		jobExecutor.shutdownNow();
		jobScheduler.stop();
		logger.debug("JobController shutdown OK!");
		return true;
	}

	class JobControllerWorker extends Thread{
		
		public void run(){
			try {
				while(!Thread.interrupted()){
					Job job = jobQueue.take();
					logger.debug("Execute = "+job);
					runningJobList.put(job.getId(), job);
					jobExecutor.execute(job);
				}
			} catch (InterruptedException e) {
				logger.debug(this.getClass().getName()+" is interrupted.");
			}
				
		}
	}
	
}
