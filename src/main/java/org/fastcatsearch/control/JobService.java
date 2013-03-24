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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.object.IndexingResult;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SearchJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author sangwook.song
 *
 */

public class JobService extends AbstractService implements JobExecutor {
	private static Logger logger = LoggerFactory.getLogger(JobService.class);
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	private BlockingQueue<Job> jobQueue; 
	private Map<Long, ResultFuture> resultFutureMap;
	private Map<Long, Job> runningJobList;
	private AtomicLong jobId;
	
	private ThreadPoolExecutor jobExecutor;
	private ThreadPoolExecutor searchJobExecutor;
	private ThreadPoolExecutor otherJobExecutor;
	private ScheduledThreadPoolExecutor scheduledJobExecutor;
	private ScheduledThreadPoolExecutor otherScheduledJobExecutor;
	
	private JobControllerWorker worker;
	private JobScheduler jobScheduler;
	private IndexingMutex indexingMutex;
	private boolean useJobScheduler;
	
	private static JobService instance;
	
	public static JobService getInstance(){
		return instance;
	}
	public void asSingleton() {
		instance = this;
	}
	public JobService(Environment environment, Settings settings, ServiceManager serviceManager){
		super(environment, settings, serviceManager);
	}
	
	protected boolean doStart() throws ServiceException {
		jobId = new AtomicLong();
		resultFutureMap = new ConcurrentHashMap<Long, ResultFuture>();
		runningJobList = new ConcurrentHashMap<Long, Job>();
		jobQueue = new LinkedBlockingQueue<Job>();
		indexingMutex = new IndexingMutex();
		jobScheduler = new JobScheduler();
		
		int executorMaxPoolSize = settings.getInt("pool.max");
		
		int indexJobMaxSize = 100;
		int searchJobMaxSize = 100;
		int otherJobMaxSize = 100;
		
		jobExecutor = ThreadPoolFactory.newCachedThreadPool("IndexJobExecutor", executorMaxPoolSize);
//		searchJobExecutor = ThreadPoolFactory.newUnlimitedCachedDaemonThreadPool("SearchJobExecutor");
//		otherJobExecutor = ThreadPoolFactory.newUnlimitedCachedDaemonThreadPool("OtherJobExecutor");
		scheduledJobExecutor = ThreadPoolFactory.newScheduledThreadPool("ScheduledIndexJobExecutor");
//		otherScheduledJobExecutor = ThreadPoolFactory.newScheduledThreadPool("OtherScheduledJobExecutor");
		
		worker = new JobControllerWorker();
		worker.start();
		if(useJobScheduler){
			jobScheduler.start();
		}
		
		return true;
	}
	
	protected boolean doStop() {
		logger.debug(getClass().getName()+" stop requested.");
		worker.interrupt();
		resultFutureMap.clear();
		jobQueue.clear();
		runningJobList.clear();
		jobExecutor.shutdownNow();
		if(useJobScheduler){
			jobScheduler.stop();
		}
		return true;
	}
	protected boolean doClose() {
		return true;
	}
	
	public void setUseJobScheduler(boolean useJobScheduler){
		this.useJobScheduler = useJobScheduler;
	}
	
	public Collection<Job> getRunningJobs(){
		return runningJobList.values();
	}
	public Collection<String> getIndexingList(){
		return indexingMutex.getIndexingList();
	}
	public void setSchedule(String key, String jobClassName, String args, Timestamp startTime, int period, boolean isYN) throws SettingException{
		if(!useJobScheduler){
			return;
		}
		
		jobScheduler.setSchedule(key, jobClassName, args, startTime, period, isYN);
	}
	
	public boolean reloadSchedules() throws SettingException{
		if(!useJobScheduler){
			return false;
		}
		
		return jobScheduler.reload();
	}
	
	public boolean toggleIndexingSchedule(String collection, String type, boolean isActive){
		if(!useJobScheduler){
			return false;
		}
		
		return jobScheduler.reloadIndexingSchedule(collection, type, isActive);
	}
	public ThreadPoolExecutor getJobExecutor(){
		return jobExecutor;
	}
	
	public ResultFuture offer(Job job) {
		job.setEnvironment(environment);
		job.setJobExecutor(this);
		
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
			DBService dbHandler = DBService.getInstance();
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
			ResultFuture resultFuture = new ResultFuture(myJobId, resultFutureMap);
			resultFutureMap.put(myJobId, resultFuture);
			job.setId(myJobId);
			jobQueue.offer(job);
			return resultFuture;
		}
	}
	
	public void result(long jobId, Job job, Object result, boolean isSuccess, long st, long et) {
		
		ResultFuture resultFuture = resultFutureMap.remove(jobId);
		runningJobList.remove(jobId);
		logger.debug("### ResultFuture = {} / map={} / job={} / result={} / success= {}", new Object[]{resultFuture, resultFutureMap.size(), job.getClass().getSimpleName(), result, isSuccess});
//		if(isManager){
			if(!(job instanceof SearchJob) || !isSuccess){
//				DBService dbHandler = DBService.getInstance();
//				String jobArgs = "";
//				if(job.getArgs() != null){
//					String[] args = job.getStringArrayArgs();
//					for (int i = 0; i < args.length; i++) {
//						jobArgs += (args[i] + " ");
//					}
//				}
//				
//				String resultStr = "";
//				if(result != null){
//					resultStr = result.toString();
//				}
//				if(dbHandler.JobHistory != null){
//					dbHandler.JobHistory.insert(jobId, job.getClass().getName(), jobArgs, isSuccess, resultStr, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
//				}
//				dbHandler.commit();
			}
			
			//
			// FIXME 색인서버와 DB정보 입력서버(마스터)는 다를수 있으므로, JobService에서 DB에 직접입력하지 않는다. 호출한 Job에서 수행.
			//
			if(job instanceof FullIndexJob || job instanceof IncIndexJob){
				indexingMutex.release(jobId);
//				DBService dbHandler = DBService.getInstance();
				String collection = job.getStringArgs(0);
				logger.debug("job="+job+", "+collection);
				
				String indexingType = "-";
				if(job instanceof FullIndexJob){
					indexingType = "F";
					//전체색인시는 증분색인 정보를 클리어해준다.
//					dbHandler.IndexingResult.delete(collection, "I");
				}else if(job instanceof IncIndexJob){
					indexingType = "I";
				}
//				int status = isSuccess ? IndexingResult.STATUS_SUCCESS : IndexingResult.STATUS_FAIL;
//				if(result instanceof IndexingResult){
//					IndexingResult jobResultIndex = (IndexingResult)result; 
//					dbHandler.IndexingResult.updateOrInsert(collection, indexingType, status, jobResultIndex.docSize, jobResultIndex.updateSize, jobResultIndex.deleteSize, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
//					dbHandler.IndexingHistory.insert(collection, indexingType, isSuccess, jobResultIndex.docSize, jobResultIndex.updateSize, jobResultIndex.deleteSize, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
//				}else{
//					dbHandler.IndexingResult.updateOrInsert(collection, indexingType, status, 0, 0, 0, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
//					dbHandler.IndexingHistory.insert(collection, indexingType, isSuccess, 0, 0, 0, job.isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
//				}
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
		
		if(resultFuture != null){
			resultFuture.put(result, isSuccess);
		}else{
			//시간초과로 ResultFutuer.poll에서 미리제거된 경우. 
			logger.debug("result arrived but future object is already removed due to timeout. result={}", result);
		}
			
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
